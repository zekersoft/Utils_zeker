package com.future.common.utils;

import com.sun.deploy.util.ArrayUtil;
import org.springblade.core.tool.utils.CollectionUtil;
import org.springblade.core.tool.utils.StringUtil;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by zeker
 *
 * @Description 关闭被占用端口
 */
public class PortUtil {
    /**
     * 查看端口PID命令前缀
     */
    private static final String PORT_PID = "cmd /c netstat -aon|findstr ";
    /**
     * 查看PID对应进程命令前缀
     */
    private static final String PID_PROCESS = "cmd /c tasklist|findstr ";
    /**
     * 关闭PID对应进程命令前缀
     */
    private static final String CLOES_PROCESS = "taskkill /T /F /PID ";
    

    /**
     * @param port 被占用的端口号
     *
     * 1.获取端口对应的PID
     */
    public static String getPortPID(String port){

        List<String[]> pids = returnInfo(PORT_PID + port);

        if (CollectionUtil.isEmpty(pids)){
            JOptionPane.showMessageDialog(new JDialog(),"端口 " + port + " 未启用，点击终止程序");
            System.exit(0);//程序终止
        }

        String[] strings = pids.get(0);

        return strings[strings.length-1];
    }

    /**
     * @param portPID 端口对应的PID
     *
     * 查看指定 PID 的进程
     */
    public static List<String> getProcess(String portPID){

        List<String[]> processes = returnInfo(PID_PROCESS + portPID);
        List<String> processNames = processes.stream().map(strings -> strings[0]).collect(Collectors.toList());//数组下标为0的为进程名称
        return processNames;
    }

    /**
     * @param portPID 端口对应的PID
     *
     *  2. 关闭PID对应的进程,也就是关闭端口
     */
    public static String cloesProcess(String portPID){

        List<String[]> strings = returnInfo(CLOES_PROCESS + portPID);

        String msg = strings.stream().map(s -> ArrayUtil.arrayToString(s)).collect(Collectors.joining());//将返回信息拼接成一条字符串

        return msg;
    }

    /**
     * @param port 端口号
     * @Description 查看被占用的端口号是什么进程
     */
    public static String CheckProcess(String port){

        return getProcess(getPortPID(port)).toString();

    }

    /**
     * @param port 端口号
     * @Description 关闭被占用的端口号
     */
    public static String cloesPort(String port){
        // if (StringUtil.isEmpty(port)){
        //     return "请输入端口号";
        // }

        String processNames = CheckProcess(port);//显示端口号哪些进程
        int n = JOptionPane.showConfirmDialog(null, processNames,"以下是端口占用进程,是否关闭？",JOptionPane.CANCEL_OPTION);
        if(n == JOptionPane.YES_OPTION ){
            return cloesProcess(getPortPID(port));
        }

        return null;


    }


    /**
     * cmd命令返回信息的方法抽取
     */
    public static List<String[]> returnInfo(String cmd){

        StringBuilder sb =new StringBuilder();
        List<String> cmdReturnInfo = new ArrayList<>();
        BufferedReader bufferedReader = null;
        Process process =null;

        //查询端口的PID命令

        // System.out.println(portPid);
        try {
            process = Runtime.getRuntime().exec(cmd);
            bufferedReader=new BufferedReader(new InputStreamReader(process.getInputStream(), Charset.forName("GBK")));
            String line;
            while((line=bufferedReader.readLine())!=null)
            {
                sb.append(line+"\n");
                cmdReturnInfo.add(line);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }finally {//关闭流

            if (bufferedReader!=null){
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (process != null) {
                process.destroy();
            }
        }
        // 匹配多个拆分由多个空格隔开的字符串【"\s"为正则表达式，\s表示匹配任何空白字符，包括空格、制表符、换页符等等。】
        List<String[]> collectList = cmdReturnInfo.stream().map(s -> s.split("\\s+")).collect(Collectors.toList());
        //collectList中的每一个元素是一个数组，一个数组代表cmd命令返回的一行数据

        return collectList;
    }


    public static void main(String[] args) {//测试8000端口

        String port = JOptionPane.showInputDialog("请输入需要关闭的端口号");
        if (StringUtil.isNotBlank(port)){
            String msg = cloesPort(port);//关闭QQ
            if (msg != null){
                JOptionPane.showMessageDialog(new JDialog(),msg);
            }
            main(args);
        }
        System.exit(0);
    }
}
