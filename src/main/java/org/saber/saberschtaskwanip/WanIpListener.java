package org.saber.saberschtaskwanip;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import sun.rmi.runtime.Log;

import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by saber on 2019/5/6.
 */
@Component
public class WanIpListener {

    private static Logger log = LoggerFactory.getLogger(WanIpListener.class);

    private static WanIpListener wanIpListener = null;

    private WanIpListener() {
    }

    public static WanIpListener getInstance() {
        if (wanIpListener != null) {
            return wanIpListener;
        }
        return new WanIpListener();
    }

    private static String LOCAL_WAN_IP = null;


    /**
     * 有两种情况需要发送邮件
     * 1.初始化
     * 1.本地IP与主机IP不同
     * 2.主机IP无法获取，此时需要更新应用程序代码
     * @param javaMailSender
     */
    public void start(JavaMailSender javaMailSender) {

        String wanIp = getWanIp();
        if (StringUtils.isEmpty(wanIp)) {
            //未获取主机IP
            this.setMail(javaMailSender, MailMsgType.GET_WANIP_FAIL, wanIp);
            return;
        }

        if (LOCAL_WAN_IP == null) {
            // 初始化或服务重启
            this.setMail(javaMailSender, MailMsgType.INIT, wanIp);
            return;
        }

        if (!wanIp.equals(LOCAL_WAN_IP)) {
            //主机IP变更
            this.setMail(javaMailSender, MailMsgType.WAN_IP_CHANGE, wanIp);
            return;
        }
    }

    public enum MailMsgType {
        INIT, GET_WANIP_FAIL, WAN_IP_CHANGE;
    }

    /**
     * 通过爬虫获取广域网IP
     * 网站：https://ip.cn/
     *
     * @return
     */
    public String getWanIp() {
        String wanIp = "";
        final String urlStr = "https://ip.cn/";
        URL url = null;
        HttpURLConnection urlConnection = null;
        StringBuilder body = new StringBuilder();
        String read = "";
        BufferedReader in = null;
        try {
            url = new URL(urlStr);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");
            in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), "UTF-8"));
            while ((read = in.readLine()) != null) {
                body.append(read + "\r\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("请求获取广域网IP失败", e);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (Exception e) {
                }
            }
        }
        log.debug("网页查询结果：\n" + body.toString());
        Pattern p = Pattern.compile("\\<div id\\=\"result\".*?\\<code>(\\d+\\.){3}\\d+\\<\\/code>.*?\\<\\/div>");
        Matcher m = p.matcher(body.toString());
        if (m.find()) {
            String ipResult = m.group(0);
            p = Pattern.compile("(\\d+\\.){3}\\d+");
            m = p.matcher(ipResult);
            log.debug("IP结果块：\n" + ipResult);
            if (m.find()) {
                wanIp = m.group(0);
                log.info("IP：" + wanIp);
            }

        }
        return wanIp;
    }

    /**
     * 定时任务无法注入
     */
    public void setMail(JavaMailSender javaMailSender, MailMsgType mailMsgType, String wanIp) {
        MimeMessage message = null;
        String hostName = "";
        try {
            message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setFrom("707639014@qq.com");
            String[] tos = {"707639014@qq.com", "2490580722@qq.com"};
            helper.setTo(tos);

            hostName = InetAddress.getLocalHost().getHostName();
            helper.setSubject("【Saber-主机IP监控】 --> 主机："+ hostName);
            helper.setText(genWanIpMailMsg(mailMsgType, wanIp));
            javaMailSender.send(message);
            setLocalWanIp(mailMsgType, wanIp);
        } catch (MessagingException e) {
            log.error("邮件发送失败", e);
        } catch (UnknownHostException e) {
            log.error("获取主机信息失败", e);
        }
    }

    /**
     * 生成消息主体
     * @param type
     * @return
     */
    public String genWanIpMailMsg(MailMsgType type, String wanIp) {
        StringBuilder msg = new StringBuilder();
        switch (type) {
            case INIT:
                msg.append("服务监控启动，IP：" + wanIp);
                break;
            case GET_WANIP_FAIL:
                msg.append("获取公网IP失败，请更新服务");
                break;
            case WAN_IP_CHANGE:
                msg.append("原IP：" + LOCAL_WAN_IP + "\n新IP：" + wanIp);
                break;
            default:
                msg.append("意料之外的错误，请更新服务");
                break;
        }

        return msg.toString();
    }

    /**
     * 设置本地IP
     * @param type
     * @param wanIp
     */
    public void setLocalWanIp(MailMsgType type, String wanIp) {
        switch (type) {
            case INIT:
                LOCAL_WAN_IP = wanIp;
                break;
            case GET_WANIP_FAIL:
                break;
            case WAN_IP_CHANGE:
                LOCAL_WAN_IP = wanIp;
                break;
            default:
                break;
        }
    }

}
