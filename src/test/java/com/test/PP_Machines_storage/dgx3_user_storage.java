package com.test.PP_Machines_storage;

import com.jcraft.jsch.*;
import org.junit.Test;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class dgx3_user_storage {

    @Test
    public void testStorageDetails() {
        JSch jsch = new JSch();
        com.jcraft.jsch.Session session = null;
        try {
            String user = "hbp";  
            String host = "dgx3.humanbrain.in";
            String password = "Health#123"; 
            int port = 22;
            session = jsch.getSession(user, host, port);
            session.setPassword(password);
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect();
            Channel channel = session.openChannel("exec");

            // Prepare the command with sudo -S
            String command = "cd /home/users && echo " + password + " | sudo -S du -sh *";
            ((ChannelExec) channel).setCommand(command);
            channel.setInputStream(null);
            ((ChannelExec) channel).setErrStream(System.err);
            InputStream in = channel.getInputStream();
            OutputStream out = channel.getOutputStream();

            channel.connect();

            // Send the password to sudo
            out.write((password + "\n").getBytes());
            out.flush();

            byte[] tmp = new byte[1024];
            StringBuilder output = new StringBuilder();
            while (true) {
                while (in.available() > 0) {
                    int i = in.read(tmp, 0, 1024);
                    if (i < 0) break;
                    output.append(new String(tmp, 0, i));
                }
                if (channel.isClosed()) {
                    if (in.available() > 0) continue;
                    System.out.println("Exit status: " + channel.getExitStatus());
                    break;
                }
                try {
                    Thread.sleep(1000);
                } catch (Exception ee) {
                    ee.printStackTrace();
                }
            }

            System.out.println("Command Output:");
            String commandOutput = output.toString();
            System.out.println(commandOutput);

            checkAndSendEmail(commandOutput);

            channel.disconnect();
            session.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Test encountered an exception: " + e.getMessage());
        }
    }

    private void checkAndSendEmail(String commandOutput) {
        String[] lines = commandOutput.split("\n");

        for (String line : lines) {
            String[] parts = line.split("\\s+");
            if (parts.length == 2) {
                String sizeStr = parts[0];
                String userDetail = parts[1];
                String[] userParts = userDetail.split("-");
                String user = userParts[0];
                String email = getEmailForUser(user);

                if (email != null && isSizeExceedingThreshold(sizeStr, "2.0T")) {
                    sendEmailAlert(user, sizeStr, email);
                } else if (email == null) {
                    System.out.println("No email found for user: " + user);
                }
            }
        }
    }

    private boolean isSizeExceedingThreshold(String sizeStr, String thresholdStr) {
        double size = parseSize(sizeStr);
        double threshold = parseSize(thresholdStr);
        return size > threshold;
    }

    private double parseSize(String sizeStr) {
        double size = Double.parseDouble(sizeStr.substring(0, sizeStr.length() - 1));
        char unit = sizeStr.charAt(sizeStr.length() - 1);

        switch (unit) {
            case 'K': return size / (1024 * 1024);   // Convert KB to TB
            case 'M': return size / (1024 * 1024);   // Convert MB to TB
            case 'G': return size / 1024;            // Convert GB to TB
            case 'T': return size;                   // TB is already in TB
            default: return 0;
        }
    }

    private String getEmailForUser(String user) {
        switch (user) {
            case "aditya": return "";
            case "akash": return "ee21s056@smail.iitm.ac.in";
            case "appUser": return "gayathri@htic.iitm.ac.in";
            case "arihant": return "ee21s061@smail.iitm.ac.in";
            case "arunima": return "ee21s062@smail.iitm.ac.in";
            case "ayantika": return "ee19d422@smail.iitm.ac.in";
            case "aziz": return "azizahammed.a@htic.iitm.ac.in";
            case "bhumika": return "ee21s060@smail.iitm.ac.in";
            case "fahim": return "ee21s050@smail.iitm.ac.in";
            case "jayraj": return "";
            case "keerthi": return "keerthi@htic.iitm.ac.in";
            case "koushik": return "ee21s055@smail.iitm.ac.in";
            case "mansi": return "ee21s063@smail.iitm.ac.in ";
            case "nathan" : return "nathan.i@htic.iitm.ac.in";
            case "nitish" : return "21f1007033@ds.study.iitm.ac.in";
            case "pralay": return "";
            case "raja": return " ee22s042@smail.iitm.ac.in";
            case "rajnish": return "0rajnishk@gmail.com";
            case "rashmi": return "ee20s051@smail.iitm.ac.in";
            case "sam": return "chrislinesam@htic.iitm.ac.in";
            case "sambhav": return "22f3003227@ds.study.iitm.ac.in";
            case "sankalp": return "";
            case "sasi": return "sasikumar.c@htic.iitm.ac.in";
            case "sathes": return "satheskumar@htic.iitm.ac.in";
            case "shailesh": return "shailxiitm@gmail.com";
            case "shubham": return "";
            case "siva": return "sivathanun@htic.iitm.ac.in";
            case "sriram": return "sriramv@htic.iitm.ac.in";
            case "supriti": return "supriti-supriti@htic.iitm.ac.in";
            
            default: return null;
        }
    }

    private void sendEmailAlert(String user, String size, String to) {
        String from = "gayathri@htic.iitm.ac.in";
        String host = "smtp.gmail.com";
        Properties properties = System.getProperties();
        properties.put("mail.smtp.host", host);
        properties.put("mail.smtp.port", "465");
        properties.put("mail.smtp.ssl.enable", "true");
        properties.put("mail.smtp.auth", "true");
        javax.mail.Session session = javax.mail.Session.getInstance(properties, new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication("gayathri@htic.iitm.ac.in", "Gayu@0918");
            }
        });
        session.setDebug(true);
        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
            message.setSubject("DGX3 Machine Storage Alert ⚠️");
            message.setText("This is an automatically generated email.\n\n" +
                    "Dear " + user + ",\n\n" +
                    "You are using " + size + " of storage on the DGX3 machine, which exceeds the 2.0T threshold.\n\n" +
                    "Please take necessary actions to reduce your storage usage.\n\n" +
                    "Thank you.");
            System.out.println("sending...");
            Transport.send(message);
            System.out.println("Sent message successfully to " + to);
        } catch (MessagingException mex) {
            mex.printStackTrace();
        }
    }
}
