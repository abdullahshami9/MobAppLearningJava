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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONObject;

public class CodeExecutor {
    private Context context;
    private ExecutorService executorService;
    private Handler mainHandler;
    private Map<String, String> environmentVariables;
    private String currentWorkingDirectory;
    private boolean isLocalExecutionEnabled = false;
    
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

    public interface TerminalExecutionCallback {
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

    /**
     * Execute terminal command for different operating systems with mobile support
     * 
     * @param command The command to execute
     * @param osType The target OS (windows, linux, mac)
     * @param callback Callback for result
     */
    public void executeTerminalCommand(String command, String osType, TerminalExecutionCallback callback) {
        executorService.execute(() -> {
            ExecutionResult result = new ExecutionResult();
            result.status = ExecutionStatus.EXECUTING;
            
            try {
                if (isLocalExecutionEnabled && canExecuteOnDevice()) {
                    // Try to execute locally if enabled and possible
                    executeCommandLocally(command, result);
                } else {
                    // Otherwise simulate command execution
                    simulateCommandExecution(command, osType, result);
                }
                
                result.status = ExecutionStatus.SUCCESS;
            } catch (Exception e) {
                result.error = e.getMessage();
                result.status = ExecutionStatus.ERROR;
            }
            
            mainHandler.post(() -> callback.onExecutionComplete(result));
        });
    }
    
    /**
     * Check if we can execute commands on the actual device
     */
    private boolean canExecuteOnDevice() {
        try {
            // Try a simple command to check if we have terminal access
            Process process = Runtime.getRuntime().exec("echo test");
            int exitCode = process.waitFor();
            return exitCode == 0;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Execute command locally on the device
     */
    private void executeCommandLocally(String command, ExecutionResult result) throws Exception {
        // Prepare environment variables
        String[] envp = null;
        if (environmentVariables != null && !environmentVariables.isEmpty()) {
            envp = new String[environmentVariables.size()];
            int i = 0;
            for (Map.Entry<String, String> entry : environmentVariables.entrySet()) {
                envp[i++] = entry.getKey() + "=" + entry.getValue();
            }
        }
        
        // Prepare working directory
        File workingDir = null;
        if (currentWorkingDirectory != null && !currentWorkingDirectory.isEmpty()) {
            workingDir = new File(currentWorkingDirectory);
            if (!workingDir.exists() || !workingDir.isDirectory()) {
                workingDir = null;
            }
        }
        
        // Execute command
        Process process = Runtime.getRuntime().exec(command, envp, workingDir);
        
        // Read output
        try (BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));
             BufferedReader stdError = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
            
            StringBuilder outputBuilder = new StringBuilder();
            StringBuilder errorBuilder = new StringBuilder();
            
            String s;
            while ((s = stdInput.readLine()) != null) {
                outputBuilder.append(s).append("\n");
            }
            
            while ((s = stdError.readLine()) != null) {
                errorBuilder.append(s).append("\n");
            }
            
            // Wait for process to complete
            int exitValue = process.waitFor();
            
            result.output = outputBuilder.toString();
            result.error = errorBuilder.toString();
            
            if (exitValue != 0 && result.error.isEmpty()) {
                result.error = "Process exited with code " + exitValue;
            }
        }
    }
    
    /**
     * Simulate command execution when local execution isn't possible
     */
    private void simulateCommandExecution(String command, String osType, ExecutionResult result) {
        String simulated;
        
        switch (osType.toLowerCase()) {
            case "windows":
                simulated = simulateWindowsCommand(command);
                break;
            case "mac":
                simulated = simulateUnixCommand(command, true);
                break;
            case "linux":
            default:
                simulated = simulateUnixCommand(command, false);
                break;
        }
        
        result.output = simulated;
    }
    
    /**
     * Simulate Windows command execution
     */
    private String simulateWindowsCommand(String command) {
        StringBuilder output = new StringBuilder();
        
        // Extract command name
        String[] parts = command.trim().split("\\s+", 2);
        String cmd = parts[0].toLowerCase();
        String args = parts.length > 1 ? parts[1] : "";
        
        switch (cmd) {
            case "dir":
                output.append(" Volume in drive C is Windows\n");
                output.append(" Volume Serial Number is XXXX-XXXX\n\n");
                output.append(" Directory of C:\\Users\\User\\Documents\n\n");
                output.append("12/01/2023  08:30 AM    <DIR>          .\n");
                output.append("12/01/2023  08:30 AM    <DIR>          ..\n");
                output.append("11/28/2023  10:23 AM             8,192 document.txt\n");
                output.append("11/27/2023  09:45 AM    <DIR>          project\n");
                output.append("11/25/2023  11:15 AM             2,048 readme.md\n");
                output.append("               3 File(s)         10,240 bytes\n");
                output.append("               3 Dir(s)  54,184,230,912 bytes free\n");
                break;
            case "cd":
                output.append("Changed directory to ").append(args.isEmpty() ? "C:\\Users\\User" : args).append("\n");
                break;
            case "echo":
                output.append(args).append("\n");
                break;
            case "type":
                if (args.isEmpty()) {
                    output.append("The syntax of the command is incorrect.\n");
                } else {
                    output.append("Content of ").append(args).append(":\n");
                    output.append("This is a simulated file content.\n");
                }
                break;
            case "systeminfo":
                output.append("HOST NAME:                 MOBILE-DEVICE\n");
                output.append("OS NAME:                   Microsoft Windows 10 Pro\n");
                output.append("OS VERSION:                10.0.19044\n");
                output.append("SYSTEM TYPE:               x64-based PC\n");
                output.append("PROCESSOR:                 Intel(R) Core(TM) i7 CPU @ 2.60GHz\n");
                output.append("BIOS VERSION:              MOBILE.123.456.789\n");
                output.append("TOTAL PHYSICAL MEMORY:     8,192 MB\n");
                break;
            default:
                if (cmd.equals("cls")) {
                    // Just return empty string for cls
                    return "";
                }
                output.append("'").append(cmd).append("' is not recognized as an internal or external command,\n");
                output.append("operable program or batch file.\n");
                break;
        }
        
        return output.toString();
    }
    
    /**
     * Simulate Unix command execution (Linux/Mac)
     */
    private String simulateUnixCommand(String command, boolean isMac) {
        StringBuilder output = new StringBuilder();
        
        // Extract command name
        String[] parts = command.trim().split("\\s+", 2);
        String cmd = parts[0];
        String args = parts.length > 1 ? parts[1] : "";
        
        switch (cmd) {
            case "ls":
                if (args.contains("-l")) {
                    output.append("total 20\n");
                    output.append("drwxr-xr-x  2 user group 4096 Dec  1 08:30 .\n");
                    output.append("drwxr-xr-x 10 user group 4096 Dec  1 08:30 ..\n");
                    output.append("-rw-r--r--  1 user group 8192 Nov 28 10:23 document.txt\n");
                    output.append("drwxr-xr-x  4 user group 4096 Nov 27 09:45 project\n");
                    output.append("-rw-r--r--  1 user group 2048 Nov 25 11:15 readme.md\n");
                } else {
                    output.append("document.txt  project  readme.md\n");
                }
                break;
            case "cd":
                output.append("Changed directory to ").append(args.isEmpty() ? "/home/user" : args).append("\n");
                break;
            case "echo":
                output.append(args).append("\n");
                break;
            case "cat":
                if (args.isEmpty()) {
                    output.append("cat: missing operand\n");
                } else {
                    output.append("Content of ").append(args).append(":\n");
                    output.append("This is a simulated file content.\n");
                }
                break;
            case "pwd":
                output.append("/home/user/documents\n");
                break;
            case "uname":
                if (args.contains("-a")) {
                    if (isMac) {
                        output.append("Darwin MacBook-Pro.local 20.6.0 Darwin Kernel Version 20.6.0: Mon Aug 30 06:12:21 PDT 2023; root:xnu-7195.141.6~3/RELEASE_X86_64 x86_64\n");
                    } else {
                        output.append("Linux android-device 5.10.81-android12-9-00001-gd88744ea9cc3-ab8827094 #1 SMP PREEMPT Thu Nov 18 15:51:33 UTC 2023 aarch64 Android\n");
                    }
                } else {
                    output.append(isMac ? "Darwin\n" : "Linux\n");
                }
                break;
            case "clear":
                // Just return empty string for clear
                return "";
            default:
                output.append("-bash: ").append(cmd).append(": command not found\n");
                break;
        }
        
        return output.toString();
    }
    
    /**
     * Sets whether to try local command execution
     */
    public void setLocalExecutionEnabled(boolean enabled) {
        this.isLocalExecutionEnabled = enabled;
    }
    
    /**
     * Set current working directory for commands
     */
    public void setCurrentWorkingDirectory(String directory) {
        this.currentWorkingDirectory = directory;
    }
    
    /**
     * Set environment variables for commands
     */
    public void setEnvironmentVariables(Map<String, String> variables) {
        this.environmentVariables = variables;
    }
} 