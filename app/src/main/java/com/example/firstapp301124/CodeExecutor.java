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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CodeExecutor {
    private Context context;
    private ExecutorService executorService;
    private Handler mainHandler;

    public interface CodeExecutionCallback {
        void onExecutionComplete(String output, String error);
    }

    public CodeExecutor(Context context) {
        this.context = context;
        this.executorService = Executors.newSingleThreadExecutor();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    public void executeCode(String code, String extension, CodeExecutionCallback callback) {
        executorService.execute(() -> {
            String output = "";
            String error = "";

            try {
                switch (extension.toLowerCase()) {
                    case ".js":
                        executeJavaScript(code, callback);
                        return;
                    case ".py":
                        output = executePython(code);
                        break;
                    case ".java":
                        output = executeJava(code);
                        break;
                    case ".php":
                        output = executePHP(code);
                        break;
                    default:
                        error = "Unsupported language: " + extension;
                }
            } catch (Exception e) {
                error = e.getMessage();
            }

            final String finalOutput = output;
            final String finalError = error;
            mainHandler.post(() -> callback.onExecutionComplete(finalOutput, finalError));
        });
    }

    private void executeJavaScript(String code, CodeExecutionCallback callback) {
        mainHandler.post(() -> {
            WebView webView = new WebView(context);
            webView.getSettings().setJavaScriptEnabled(true);

            // Create a StringBuilder to store console.log output
            StringBuilder output = new StringBuilder();

            // Add JavaScript interface to capture console.log
            class JSInterface {
                @JavascriptInterface
                public void log(String message) {
                    output.append(message).append("\n");
                }
            }

            webView.addJavascriptInterface(new JSInterface(), "Android");

            // Inject console.log override
            String wrappedCode = "console.log = function(message) { Android.log(message.toString()); };" + code;

            webView.setWebViewClient(new WebViewClient() {
                @Override
                public void onPageFinished(WebView view, String url) {
                    // Execute the code after page is loaded
                    view.evaluateJavascript(wrappedCode, null);
                    // Give a small delay for execution and then return the output
                    mainHandler.postDelayed(() -> 
                        callback.onExecutionComplete(output.toString(), ""), 500);
                }
            });

            webView.loadData("<html><body></body></html>", "text/html", "UTF-8");
        });
    }

    private String executePython(String code) throws Exception {
        // Create a temporary file
        File tempFile = File.createTempFile("temp_python", ".py", context.getCacheDir());
        try (FileWriter writer = new FileWriter(tempFile)) {
            writer.write(code);
        }

        // Execute Python code
        ProcessBuilder processBuilder = new ProcessBuilder("python", tempFile.getAbsolutePath());
        Process process = processBuilder.start();
        
        // Read output
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        StringBuilder output = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            output.append(line).append("\n");
        }

        // Check for errors
        BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
        StringBuilder error = new StringBuilder();
        while ((line = errorReader.readLine()) != null) {
            error.append(line).append("\n");
        }

        // Clean up
        tempFile.delete();

        if (error.length() > 0) {
            throw new Exception(error.toString());
        }

        return output.toString();
    }

    private String executeJava(String code) throws Exception {
        // Extract class name from code
        String className = "Main"; // Default class name
        String[] lines = code.split("\n");
        for (String line : lines) {
            if (line.contains("class ")) {
                className = line.split("class ")[1].split("\\s|\\{")[0];
                break;
            }
        }

        // Create a temporary file
        File tempFile = File.createTempFile(className, ".java", context.getCacheDir());
        try (FileWriter writer = new FileWriter(tempFile)) {
            writer.write(code);
        }

        // Compile Java code
        ProcessBuilder compileBuilder = new ProcessBuilder("javac", tempFile.getAbsolutePath());
        Process compileProcess = compileBuilder.start();
        compileProcess.waitFor();

        // Check for compilation errors
        BufferedReader errorReader = new BufferedReader(new InputStreamReader(compileProcess.getErrorStream()));
        StringBuilder error = new StringBuilder();
        String line;
        while ((line = errorReader.readLine()) != null) {
            error.append(line).append("\n");
        }

        if (error.length() > 0) {
            throw new Exception(error.toString());
        }

        // Run Java code
        ProcessBuilder runBuilder = new ProcessBuilder("java", "-cp", context.getCacheDir().getAbsolutePath(), className);
        Process runProcess = runBuilder.start();

        // Read output
        BufferedReader reader = new BufferedReader(new InputStreamReader(runProcess.getInputStream()));
        StringBuilder output = new StringBuilder();
        while ((line = reader.readLine()) != null) {
            output.append(line).append("\n");
        }

        // Clean up
        tempFile.delete();
        new File(context.getCacheDir(), className + ".class").delete();

        return output.toString();
    }

    private String executePHP(String code) throws Exception {
        // Create a temporary file
        File tempFile = File.createTempFile("temp_php", ".php", context.getCacheDir());
        try (FileWriter writer = new FileWriter(tempFile)) {
            writer.write(code);
        }

        // Execute PHP code
        ProcessBuilder processBuilder = new ProcessBuilder("php", tempFile.getAbsolutePath());
        Process process = processBuilder.start();
        
        // Read output
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        StringBuilder output = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            output.append(line).append("\n");
        }

        // Check for errors
        BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
        StringBuilder error = new StringBuilder();
        while ((line = errorReader.readLine()) != null) {
            error.append(line).append("\n");
        }

        // Clean up
        tempFile.delete();

        if (error.length() > 0) {
            throw new Exception(error.toString());
        }

        return output.toString();
    }
} 