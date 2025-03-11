package com.example.firstapp301124;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONObject;

public class CodeExecutor {
    private Context context;
    private ExecutorService executorService;
    private Handler mainHandler;
    
    public static class ExecutionResult {
        public String output;
        public String error;
        public List<Integer> errorLines;
        public ExecutionStatus status;
        
        public ExecutionResult() {
            this.output = "";
            this.error = "";
            this.errorLines = new ArrayList<>();
            this.status = ExecutionStatus.NOT_EXECUTED;
        }
    }
    
    public enum ExecutionStatus {
        NOT_EXECUTED,
        EXECUTING,
        SUCCESS,
        ERROR
    }

    public interface CodeExecutionCallback {
        void onExecutionComplete(ExecutionResult result);
        default void onInstallationProgress(String message) {} // Optional callback for installation progress
    }

    public CodeExecutor(Context context) {
        this.context = context;
        this.executorService = Executors.newSingleThreadExecutor();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    public void executeCode(String code, String extension, CodeExecutionCallback callback) {
        ExecutionResult result = new ExecutionResult();
        result.status = ExecutionStatus.EXECUTING;
        
        try {
            switch (extension.toLowerCase()) {
                case ".js":
                    executeJavaScript(code, result, callback);
                    return;
                case ".php":
                    executePHPInWebView(code, result, callback);
                    return;
                case ".py":
                    executePythonInWebView(code, result, callback);
                    return;
                case ".java":
                    executeJavaInWebView(code, result, callback);
                    return;
                default:
                    result.error = "Unsupported file type: " + extension;
                    result.status = ExecutionStatus.ERROR;
                    result.errorLines.add(1);
            }
        } catch (Exception e) {
            result.error = e.getMessage();
            result.status = ExecutionStatus.ERROR;
            result.errorLines.add(1);
        }
        
        mainHandler.post(() -> callback.onExecutionComplete(result));
    }

    private void executeJavaScript(String code, ExecutionResult result, CodeExecutionCallback callback) {
        mainHandler.post(() -> {
            WebView webView = new WebView(context);
            webView.getSettings().setJavaScriptEnabled(true);

            class JSInterface {
                @JavascriptInterface
                public void log(String message) {
                    mainHandler.post(() -> {
                        result.output += message + "\n";
                    });
                }

                @JavascriptInterface
                public void error(String message, int lineNumber) {
                    mainHandler.post(() -> {
                        result.error += message + "\n";
                        result.errorLines.add(lineNumber);
                        result.status = ExecutionStatus.ERROR;
                        callback.onExecutionComplete(result);
                    });
                }

                @JavascriptInterface
                public void complete() {
                    mainHandler.post(() -> {
                        if (result.error.isEmpty()) {
                            result.status = ExecutionStatus.SUCCESS;
                            if (result.output.isEmpty()) {
                                result.output = "Code executed successfully.\n";
                            }
                        }
                        callback.onExecutionComplete(result);
                    });
                }
            }

            webView.addJavascriptInterface(new JSInterface(), "Android");

            String wrappedCode = 
                "try {" +
                "  let output = '';" +
                "  console.log = function(message) { " +
                "    Android.log(message.toString());" +
                "  };" +
                "  " + code + "\n" +
                "  if (output === '') { console.log('Code executed successfully.'); }" +
                "  setTimeout(function() { Android.complete(); }, 100);" +
                "} catch(e) {" +
                "  Android.error(e.message, (e.lineNumber || 1));" +
                "}";

            webView.setWebViewClient(new WebViewClient() {
                @Override
                public void onPageFinished(WebView view, String url) {
                    view.evaluateJavascript(wrappedCode, null);
                }
            });

            webView.loadData("<html><body></body></html>", "text/html", "UTF-8");
        });
    }

    private void executePythonInWebView(String code, ExecutionResult result, CodeExecutionCallback callback) {
        mainHandler.post(() -> {
            WebView webView = new WebView(context);
            webView.getSettings().setJavaScriptEnabled(true);
            
            // Load Skulpt (Python-to-JavaScript implementation)
            String html = "<html><body>" +
                "<script src='https://skulpt.org/js/skulpt.min.js'></script>" +
                "<script src='https://skulpt.org/js/skulpt-stdlib.js'></script>" +
                "<script>" +
                "window.onerror = function(msg, url, line) { " +
                "    window.Android.onError('JavaScript error: ' + msg + ' at line ' + line);" +
                "    return true;" +
                "};" +
                "window.onload = function() {" +
                "    function outf(text) { window.Android.onOutput(text); }" +
                "    function builtinRead(x) {" +
                "        if (Sk.builtinFiles === undefined || Sk.builtinFiles['files'][x] === undefined)" +
                "            throw 'File not found: ' + x;" +
                "        return Sk.builtinFiles['files'][x];" +
                "    }" +
                "    Sk.configure({" +
                "        output: outf," +
                "        read: builtinRead," +
                "        __future__: Sk.python3" +
                "    });" +
                "    try {" +
                "        Sk.importMainWithBody('<stdin>', false, " + JSONObject.quote(code) + ", true);" +
                "        window.Android.onComplete();" +
                "    } catch(e) {" +
                "        window.Android.onError(e.toString());" +
                "    }" +
                "};" +
                "</script></body></html>";

            class AndroidInterface {
                @JavascriptInterface
                public void onOutput(String output) {
                    result.output += output;
                }

                @JavascriptInterface
                public void onComplete() {
                    if (result.error.isEmpty()) {
                        result.status = ExecutionStatus.SUCCESS;
                    }
                }

                @JavascriptInterface
                public void onError(String error) {
                    result.error = error;
                    result.status = ExecutionStatus.ERROR;
                    // Parse line number from Python error message
                    try {
                        String[] parts = error.split("line ");
                        if (parts.length > 1) {
                            int lineNum = Integer.parseInt(parts[1].split("\\D")[0]);
                            result.errorLines.add(lineNum);
                        } else {
                            result.errorLines.add(1);
                        }
                    } catch (Exception e) {
                        result.errorLines.add(1);
                    }
                }
            }

            webView.addJavascriptInterface(new AndroidInterface(), "Android");
            webView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null);
        });
    }

    private void executeJavaInWebView(String code, ExecutionResult result, CodeExecutionCallback callback) {
        // For Java, we'll use a simple Java-to-JavaScript transpiler approach
        // This is a basic implementation that handles simple Java code
        mainHandler.post(() -> {
            WebView webView = new WebView(context);
            webView.getSettings().setJavaScriptEnabled(true);
            
            // Convert Java code to JavaScript (basic conversion)
            String jsCode = convertJavaToJS(code);
            
            String html = "<html><body><script>" +
                "try {" +
                "    let console = { log: function(text) { window.Android.onOutput(text + '\\n'); } };" +
                "    " + jsCode +
                "    window.Android.onComplete('');" +
                "} catch(e) {" +
                "    window.Android.onError(e.toString());" +
                "}" +
                "</script></body></html>";

            class AndroidInterface {
                @JavascriptInterface
                public void onOutput(String output) {
                    result.output += output;
                }

                @JavascriptInterface
                public void onComplete(String unused) {
                    result.status = ExecutionStatus.SUCCESS;
                }

                @JavascriptInterface
                public void onError(String error) {
                    result.error = error;
                    result.status = ExecutionStatus.ERROR;
                    result.errorLines.add(1);
                }
            }

            webView.addJavascriptInterface(new AndroidInterface(), "Android");
            webView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null);
        });
    }

    private String convertJavaToJS(String javaCode) {
        // Basic Java to JavaScript conversion
        // This is a simplified version - you might want to use a proper transpiler
        return javaCode
            .replace("System.out.println", "console.log")
            .replace("public class", "class")
            .replace("public static void main(String[] args)", "function main()");
    }

    private void executePHPInWebView(String code, ExecutionResult result, CodeExecutionCallback callback) {
        mainHandler.post(() -> {
            WebView webView = new WebView(context);
            webView.getSettings().setJavaScriptEnabled(true);
            webView.getSettings().setDomStorageEnabled(true);
            
            String html = "<html><body>" +
                "<script src='https://cdn.jsdelivr.net/npm/php-wasm@0.0.9/php-wasm.js'></script>" +
                "<script>" +
                "let isLoaded = false;" +
                "window.onerror = function(msg, url, line) { " +
                "    window.Android.onError('JavaScript error: ' + msg + ' at line ' + line);" +
                "    return true;" +
                "};" +
                "function runPHP() {" +
                "    if (!isLoaded) {" +
                "        setTimeout(runPHP, 100);" + // Wait for PHP.js to load
                "        return;" +
                "    }" +
                "    try {" +
                "        let code = " + JSONObject.quote(code) + ";" +
                "        let output = PHP.run(code);" +
                "        window.Android.onOutput(output);" +
                "        window.Android.onComplete();" +
                "    } catch(e) {" +
                "        window.Android.onError(e.toString());" +
                "    }" +
                "}" +
                "window.onload = function() {" +
                "    if (typeof PHP !== 'undefined') {" +
                "        isLoaded = true;" +
                "        runPHP();" +
                "    } else {" +
                "        window.Android.onError('Failed to load PHP interpreter');" +
                "    }" +
                "};" +
                "</script></body></html>";

            class AndroidInterface {
                @JavascriptInterface
                public void onOutput(String output) {
                    mainHandler.post(() -> {
                        if (output != null && !output.trim().isEmpty()) {
                            result.output += output;
                        }
                    });
                }

                @JavascriptInterface
                public void onComplete() {
                    mainHandler.post(() -> {
                        if (result.error.isEmpty()) {
                            result.status = ExecutionStatus.SUCCESS;
                            if (result.output.isEmpty()) {
                                result.output = "PHP code executed successfully.\n";
                            }
                        }
                        callback.onExecutionComplete(result);
                    });
                }

                @JavascriptInterface
                public void onError(String error) {
                    mainHandler.post(() -> {
                        result.error = error;
                        result.status = ExecutionStatus.ERROR;
                        // Parse line number from PHP error message
                        try {
                            String[] parts = error.split("line ");
                            if (parts.length > 1) {
                                int lineNum = Integer.parseInt(parts[1].split("\\D")[0]);
                                result.errorLines.add(lineNum);
                            } else {
                                result.errorLines.add(1);
                            }
                        } catch (Exception e) {
                            result.errorLines.add(1);
                        }
                        callback.onExecutionComplete(result);
                    });
                }
            }

            webView.setWebViewClient(new WebViewClient() {
                @Override
                public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                    result.error = "Failed to load PHP interpreter: " + description;
                    result.status = ExecutionStatus.ERROR;
                    callback.onExecutionComplete(result);
                }

                @Override
                public void onPageFinished(WebView view, String url) {
                    // Page loaded, PHP.js should start loading
                }
            });

            webView.addJavascriptInterface(new AndroidInterface(), "Android");
            webView.loadDataWithBaseURL("https://example.com", html, "text/html", "UTF-8", null);
        });
    }

    private void executePython(String code, ExecutionResult result) throws Exception {
        File tempFile = File.createTempFile("temp_python", ".py", context.getCacheDir());
        try (FileWriter writer = new FileWriter(tempFile)) {
            writer.write(code);
        }

        ProcessBuilder processBuilder = new ProcessBuilder("python", tempFile.getAbsolutePath());
        Process process = processBuilder.start();
        
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        while ((line = reader.readLine()) != null) {
            result.output += line + "\n";
        }

        BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
        while ((line = errorReader.readLine()) != null) {
            result.error += line + "\n";
            extractErrorLines(line, result.errorLines);
        }

        tempFile.delete();
    }

    private void executeJava(String code, ExecutionResult result) throws Exception {
        String className = "Main";
        String[] lines = code.split("\n");
        for (String line : lines) {
            if (line.contains("class ")) {
                className = line.split("class ")[1].split("\\s|\\{")[0];
                break;
            }
        }

        File tempFile = File.createTempFile(className, ".java", context.getCacheDir());
        try (FileWriter writer = new FileWriter(tempFile)) {
            writer.write(code);
        }

        ProcessBuilder compileBuilder = new ProcessBuilder("javac", tempFile.getAbsolutePath());
        Process compileProcess = compileBuilder.start();
        compileProcess.waitFor();

        BufferedReader errorReader = new BufferedReader(new InputStreamReader(compileProcess.getErrorStream()));
        String line;
        while ((line = errorReader.readLine()) != null) {
            result.error += line + "\n";
            extractErrorLines(line, result.errorLines);
        }

        if (result.error.isEmpty()) {
            ProcessBuilder runBuilder = new ProcessBuilder("java", "-cp", context.getCacheDir().getAbsolutePath(), className);
            Process runProcess = runBuilder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(runProcess.getInputStream()));
            while ((line = reader.readLine()) != null) {
                result.output += line + "\n";
            }
        }

        tempFile.delete();
        new File(context.getCacheDir(), className + ".class").delete();
    }

    private void extractErrorLines(String errorMessage, List<Integer> errorLines) {
        // Common patterns for error messages in different languages
        String[] patterns = {
            "line (\\d+)",            // Generic
            "Line (\\d+):",           // PHP
            "at line (\\d+)",         // JavaScript
            "line (\\d+), column \\d+", // Python
            ".java:(\\d+):"           // Java
        };

        for (String patternStr : patterns) {
            Pattern pattern = Pattern.compile(patternStr);
            Matcher matcher = pattern.matcher(errorMessage);
            while (matcher.find()) {
                try {
                    int lineNum = Integer.parseInt(matcher.group(1));
                    if (!errorLines.contains(lineNum)) {
                        errorLines.add(lineNum);
                    }
                } catch (NumberFormatException e) {
                    // Skip if line number can't be parsed
                }
            }
        }
    }
} 