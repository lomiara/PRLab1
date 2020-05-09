import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.concurrent.Semaphore;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {

    public static boolean deleteDirectory(@org.jetbrains.annotations.NotNull File directory) {

        if(directory.exists()){
            File[] files = directory.listFiles();
            if(null!=files){
                for(int i=0; i<files.length; i++) {
                    if(files[i].isDirectory()) {
                        deleteDirectory(files[i]);
                    }
                    else {
                        files[i].delete();
                    }
                }
            }
        }
        return(directory.delete());
    }
    private static void getImages() throws Exception {
        BufferedReader reader = new BufferedReader(new FileReader(new File("Uimages.txt")));
        String line;
        int i = 0;
        while ((line = reader.readLine()) != null) {
            i++;
            String url = "https://utm."+line;
            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            con.setRequestMethod("GET");
            InputStream in = con.getInputStream();
            OutputStream out = new FileOutputStream("Uimages/"+i+".jpg");
            try {
                byte[] bytes = new byte[200000];
                int length;
                while ((length = in.read(bytes)) != -1) {
                    out.write(bytes, 0, length);
                }
            } finally {
                in.close();
                out.close();
            }
        }
    }

    public static void main(String[] args) throws Exception {
        StringBuilder st = new StringBuilder(8096);
        try {
            SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
            SSLSocket socket = (SSLSocket) factory.createSocket("utm.md",443);
            OutputStream output = socket.getOutputStream();
            PrintWriter writer = new PrintWriter(output, true);
            writer.println("GET / HTTP/1.0");
            writer.println("Host: utm.md:443");
            writer.println("Connection: Close");
            writer.println("Accept: image");
            writer.println();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            int i = 0;
            while (i != -1) {
                i = bufferedReader.read();
                st.append((char) i);
            }
            FileWriter writer2 = new FileWriter("utm.txt",false);
            FileWriter writer1 = new FileWriter("Uimages.txt",false);
            writer2.write(String.valueOf(st));
            String s = ("md.([/|.|\\w|%|\\s|-|:|-])+\\.(?:jpg|gif|png)");
            Pattern pattern = Pattern.compile(s);
            Matcher matcher = pattern.matcher(st);
            while(matcher.find()) {
                writer1.write(st.substring(matcher.start(),matcher.end()));
                writer1.append('\n');
                writer1.flush();
            }
        } catch (UnknownHostException e) {
            System.out.println("Server not found " + e.getMessage());
        } catch (IOException e) {
            System.out.println("I/O error " + e.getMessage());
        }
        File file = new File("Uimages");
        deleteDirectory(file);
        file.mkdir();
        getImages();
    }
}