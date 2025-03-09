package com.example.firstapp301124;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    }

    public CodeExecutor(Context context) {
        this.context = context;
        this.executorService = Executors.newSingleThreadExecutor();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    public void executeCode(String code, String extension, CodeExecutionCallback callback) {
        ExecutionResult result = new ExecutionResult();
        result.status = ExecutionStatus.EXECUTING;
        mainHandler.post(() -> callback.onExecutionComplete(result));

        executorService.execute(() -> {
            try {
                switch (extension.toLowerCase()) {
                    case ".js":
                        executeJavaScript(code, result, callback);
                        return;
                    case ".py":
                        executePython(code, result);
                        break;
                    case ".java":
                        executeJava(code, result);
                        break;
                    case ".php":
                        executePHP(code, result);
                        break;
                    default:
                        result.error = "Unsupported language: " + extension;
                        result.status = ExecutionStatus.ERROR;
                }
            } catch (Exception e) {
                result.error = e.getMessage();
                result.status = ExecutionStatus.ERROR;
                extractErrorLines(result.error, result.errorLines);
            }

            if (result.error.isEmpty()) {
                result.status = ExecutionStatus.SUCCESS;
            } else {
                result.status = ExecutionStatus.ERROR;
            }
            
            mainHandler.post(() -> callback.onExecutionComplete(result));
        });
    }

    private void executeJavaScript(String code, ExecutionResult result, CodeExecutionCallback callback) {
        mainHandler.post(() -> {
            WebView webView = new WebView(context);
            webView.getSettings().setJavaScriptEnabled(true);

            class JSInterface {
                @JavascriptInterface
                public void log(String message) {
                    result.output += message + "\n";
                }

                @JavascriptInterface
                public void error(String message, int lineNumber) {
                    result.error += message + "\n";
                    result.errorLines.add(lineNumber);
                }
            }

            webView.addJavascriptInterface(new JSInterface(), "Android");

            String wrappedCode = 
                "try {" +
                "  console.log = function(message) { Android.log(message.toString()); };" +
                "  " + code +
                "} catch(e) {" +
                "  Android.error(e.message, e.lineNumber);" +
                "}";

            webView.setWebViewClient(new WebViewClient() {
                @Override
                public void onPageFinished(WebView view, String url) {
                    view.evaluateJavascript(wrappedCode, null);
                    mainHandler.postDelayed(() -> {
                        result.status = result.error.isEmpty() ? 
                            ExecutionStatus.SUCCESS : ExecutionStatus.ERROR;
                        callback.onExecutionComplete(result);
                    }, 500);
                }
            });

            webView.loadData("<html><body></body></html>", "text/html", "UTF-8");
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

    private void executePHP(String code, ExecutionResult result) throws Exception {
        File tempFile = File.createTempFile("temp_php", ".php", context.getCacheDir());
        try (FileWriter writer = new FileWriter(tempFile)) {
            writer.write(code);
        }

        ProcessBuilder processBuilder = new ProcessBuilder("php", tempFile.getAbsolutePath());
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