import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Code_Counter {

    public static void main(String[] args) {
        // 指定要统计代码行数的文件夹路径
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("请选择要统计的文件夹");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int userChoose = chooser.showOpenDialog(null);
        if (userChoose == JFileChooser.APPROVE_OPTION) {
            File folder = chooser.getSelectedFile();
            // 调用countLines方法统计各文件类型的行数和个数
            Map<String, FileStats> stats = countLines(folder);
            // 输出每种文件类型的个数和行数
            for (Map.Entry<String, FileStats> entry : stats.entrySet()) {
                System.out.println(entry.getKey() +
                        "： 文件个数 = " + entry.getValue().count +", 总行数 = " + entry.getValue().totalLines +
                        ", 单行注释 = " + entry.getValue().singleLineComments +
                        ", 多行注释 = " + entry.getValue().multiLineComments +
                        ", 空行 = "+ entry.getValue().emptyLines +
                        ", 行数 = " + entry.getValue().lines);
            }
        }
    }

    // 统计文件夹内各文件类型的行数和个数
    public static Map<String, FileStats> countLines(File folder) {
        Map<String, FileStats> fileStatsMap = new HashMap<>(); // 存储每种文件类型的统计信息
        // 检查是否为目录
        if (folder.isDirectory()) {
            // 列出目录中的所有文件和文件夹
            File[] files = folder.listFiles();
            for (File file : files) {
                if (file.isDirectory()) {
                    // 如果是子文件夹，递归调用countLines
                    Map<String, FileStats> subFolderStats = countLines(file);
                    // 合并子文件夹统计结果
                    mergeStats(fileStatsMap, subFolderStats);
                } else {
                    // 检查文件类型
                    String extension = getFileExtension(file.getName());
                    if (isCodeFile(extension)) {
                        // 统计文件行数
                        FileStats fileStats = countFileLines(file);
                        // 更新统计信息
                        fileStatsMap.computeIfAbsent(extension, k -> new FileStats()).combine(fileStats);
                    }
                }
            }
        }
        return fileStatsMap; // 返回统计结果
    }

    // 检查文件后缀是否为支持的代码文件类型
    public static boolean isCodeFile(String extension) {
        return extension.equals("c") ||
                extension.equals("cpp") ||
                extension.equals("py") ||
                extension.equals("java") ||
                extension.equals("h");
    }

    // 获取文件后缀
    public static String getFileExtension(String fileName) {
        int lastIndex = fileName.lastIndexOf('.');
        return (lastIndex > 0) ? fileName.substring(lastIndex + 1).toLowerCase() : ""; // 返回小写后缀
    }

    // 合并子文件夹的统计结果
    public static void mergeStats(Map<String, FileStats> mainStats, Map<String, FileStats> subStats) {
        for (Map.Entry<String, FileStats> entry : subStats.entrySet()) {
            mainStats.computeIfAbsent(entry.getKey(), k -> new FileStats()).combine(entry.getValue());
        }
    }

    // 统计单个文件的行数等信息
    public static FileStats countFileLines(File file) {
        FileStats fileStats = new FileStats();
        boolean ismultiline = false;//标记是否在多行注释
        try (java.io.BufferedReader br = new java.io.BufferedReader(new java.io.FileReader(file))) {
            String line;
            while ((line= br.readLine())!=null){
                fileStats.totalLines++;
                //去除行首尾空白字符
                String trimmedLine = line.trim();


                //单行
                if(ismultiline){
                    fileStats.multiLineComments++;
                    if(trimmedLine.endsWith("*/")){
                        ismultiline = false;
                    }
                    continue;
                }

                if(trimmedLine.isEmpty()){
                    fileStats.emptyLines++;
                }else if(trimmedLine.startsWith("//")){
                    fileStats.singleLineComments++;
                }else if(trimmedLine.startsWith("/*")){
                    fileStats.multiLineComments++;
                    ismultiline = true;
                    if(trimmedLine.endsWith("*/")){
                        ismultiline = false;
                    }
                }else{
                    fileStats.lines++;
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        fileStats.count++;
        return fileStats;

    }

    // 文件统计信息类
    static class FileStats {
        int totalLines=0;
        int lines = 0; // 行数
        int count = 0; // 文件个数
        int singleLineComments = 0;
        int multiLineComments = 0;
        int emptyLines = 0;

        void combine(FileStats others) {
            this.lines += others.lines;
            this.count += others.count;
            this.singleLineComments += others.singleLineComments;
            this.multiLineComments += others.multiLineComments;
            this.emptyLines += others.emptyLines;
            this.totalLines += others.totalLines;
        }
    }
}