
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author GiuseppeNew
 */
public class LaunchProcess {
private static final int N_PROC = 50;

    public static void main(String[] args) {


        for(int i=0; i<N_PROC; i++) {
            System.out.println("processo: " + i + " iniziato ***************");
            System.out.flush();
            Thread thread = new Thread(new Runnable() {
                public void run() {
                    try {
                        launch( Thread.currentThread().getName());
                        System.out.println("processo: " + Thread.currentThread().getName() + " terminato **************");
//                        System.out.flush();
                    } catch (IOException ex) {
                        Logger.getLogger(LaunchProcess.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(LaunchProcess.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }, Integer.toString(i));
            thread.start();
        }
    }

    private static void launch(String id) throws IOException, InterruptedException {

//        System.out.println("aaaaaaaaaaaaa");
//            Process proc = rt.exec("cmd /C \"c:\\ciao.txt\"");
        Runtime rt = Runtime.getRuntime();
        Process proc = rt.exec("java -jar dist\\BonitaMiddleware.jar " + id);
//            proc.getOutputStream() = System.
//            System.setOut(new PrintStream(proc.getOutputStream()));
//            System.setErr(new PrintStream(proc.getErrorStream()));
        copy(proc.getInputStream(), System.out);
        proc.waitFor();
        int exitVal = proc.exitValue();

    }

    static void copy(InputStream in, OutputStream out) throws IOException {
        while (true) {
          int c = in.read();
          if (c == -1) break;
          out.write((char)c);
        }
    }

}
