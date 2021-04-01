import java.awt.EventQueue;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.io.*;
import java.net.*;
import java.util.Arrays;
import java.util.Vector;
import java.util.concurrent.TimeUnit;
import javax.swing.GroupLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.GroupLayout.Alignment;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.EventQueue;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.JLabel;

public class Server extends JFrame {

    public static boolean flag = true;
    public static int tamanhoMsgT = 0;
    public static int portaUdp;
    public static int portaUdpOrigem;
    public static int ipDestino;
    public static int inputField;
    public static String dadosUdpString = "";
    public static String timeStampPacotesString = "";
    private static JPanel contentPane;
    private static JTextArea textArea;

    Server() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 450, 300);
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(contentPane);

        JScrollPane scrollPane = new JScrollPane();
        GroupLayout gl_contentPane = new GroupLayout(contentPane);
        gl_contentPane.setHorizontalGroup(
                gl_contentPane.createParallelGroup(Alignment.LEADING)
                        .addGroup(gl_contentPane.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 414, Short.MAX_VALUE)
                                .addContainerGap())
        );
        
        gl_contentPane.setVerticalGroup(
                gl_contentPane.createParallelGroup(Alignment.LEADING)
                        .addGroup(Alignment.TRAILING, gl_contentPane.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 233, Short.MAX_VALUE)
                                .addContainerGap())
                        
                        
        );

        textArea = new JTextArea();
        scrollPane.setViewportView(textArea);
        contentPane.setLayout(gl_contentPane);
    }

    public static Runnable receiveUdp = () -> {
        //System.out.println("tamanhoMsg = " + tamanhoMsgT);
        byte[] buffer = new byte[tamanhoMsgT];
        try {
            DatagramSocket datagramSocket = new DatagramSocket(portaUdp);
            DatagramPacket datagramPacket = new DatagramPacket(buffer, buffer.length);
            datagramSocket.setSoTimeout(1000);
            int i = 0;
            while(flag){
                try{
                    datagramSocket.receive(datagramPacket);
                    timeStampPacotesString = timeStampPacotesString + System.currentTimeMillis() + " ";
                    dadosUdpString = dadosUdpString + Arrays.toString(datagramPacket.getData()) + "%%%";
                    textArea.append(datagramPacket.getPort()  + " " + Arrays.toString(datagramPacket.getData()) + "\n");

                }catch (SocketTimeoutException e) {
                    // timeout exception.
                    textArea.append("Timeout reached!!! " + e + "\n");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    };

    public static int getIndex(String s){
        String r = "";
        int i = 1;
        char aux = s.charAt(i);
        while(aux != ','){
            r = r + aux;
            i++;
            aux = s.charAt(i);
        }
        return Integer.parseInt(r);
    }

    public static void main(String[] args) throws IOException {

        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    Server frame = new Server();
                    frame.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        int portaServidor = 8888;
        int teste, nTestes, tamanhoMsg;

        String[] dadosPacotes;
        String[] timeStampPacotes;

        int pacotesRecebidos;
        int bytesRecebidos;

        InetAddress ipCliente;

        //UDP---------------------------------------------------------------------------------
        DatagramSocket serverSocketUdp;
        DatagramPacket pacoteUdp;
        byte[] buffer = new byte[0];
        //TCP---------------------------------------------------------------------------------
        ServerSocket serverSocketTcp;
        Socket conexao;
        PrintWriter saida;
        BufferedReader buferredReader;
        InputStreamReader inputStreamReader;

        serverSocketTcp = new ServerSocket(portaServidor);
        serverSocketUdp = new DatagramSocket(portaServidor);

        try{
            conexao = serverSocketTcp.accept();
            inputStreamReader = new InputStreamReader(conexao.getInputStream());
            buferredReader = new BufferedReader(inputStreamReader);

            textArea.append("Conexao TCP estabelecida com: " + conexao.getInetAddress().getHostAddress() + " na porta " + conexao.getPort() + "\n");
            //----------------------------------------------------------------------------------------------


            String tcpMsg = buferredReader.readLine();
            String[] aux;
            aux = tcpMsg.split(" ");
            teste = Integer.parseInt(aux[0]);
            nTestes = Integer.parseInt(aux[1]);
            tamanhoMsgT = Integer.parseInt(aux[2]);
            portaUdp =  Integer.parseInt(aux[3]);
            portaUdpOrigem =  Integer.parseInt(aux[4]);
            Thread thread =  new Thread(receiveUdp);
            thread.start();

            tcpMsg = buferredReader.readLine();
            flag = false;
            thread.join();
            System.out.println(tcpMsg);

            System.out.println(dadosUdpString);

            //get/send reults-------------------------------------------------------------------------------
            dadosPacotes = dadosUdpString.split("%%%");
            timeStampPacotes = timeStampPacotesString.split(" ");
            pacotesRecebidos = dadosPacotes.length;
            bytesRecebidos = pacotesRecebidos * tamanhoMsgT;
            textArea.append(Arrays.toString(dadosPacotes) + "\n");
            textArea.append(Arrays.toString(timeStampPacotes) + "\n");

            //calc jitter;
            long minJitter = Long.MAX_VALUE;
            long maxJitter = 0;
            long jitterTotal = 0;
            long ji;
            int jitCount = 0;
            double jitterMedio = 0;
            if(dadosPacotes.length > 1){
                int a;
                int b;
                for (int i = 0; i < dadosPacotes.length -1 ; i++) {
                    a = getIndex(dadosPacotes[i]);
                    b = getIndex(dadosPacotes[i+1]);
                    if(a + 1 == b){
                        jitCount = jitCount + 1;
                        ji = Long.parseLong(timeStampPacotes[i+1])  - Long.parseLong(timeStampPacotes[i]);
                        jitterTotal = jitterTotal + ji;
                        if (ji > maxJitter){
                            maxJitter = ji;
                        }
                        if(ji < minJitter){
                            minJitter = ji;
                        }
                    }
                }
                if(jitCount != 0){
                    jitterMedio = (double)jitterTotal/(double)jitCount;
                }
            }


            //----------------------------------

            saida = new PrintWriter(conexao.getOutputStream());
            textArea.append("\n---------------- RESUMO -------------------\nInformacoes a serem enviadas\n");
            textArea.append("\n------------- RESULTADOS ----------------\n");
            textArea.append("Total de Pacotes Recebidos: " + pacotesRecebidos + "\n");
            textArea.append("Total Bytes Recebidos: " + bytesRecebidos + "\n");
            textArea.append("Horario de recebimento (milissegundos):" + timeStampPacotes[timeStampPacotes.length-1] + "\n");
            textArea.append("Taxa de Transferencia: A calcular" + "\n");
            textArea.append("% de Perda de Pacotes: A calcular" + "\n");
            textArea.append("Jitter Medio: " + jitterMedio + "\n");
            textArea.append("Jitter Minimo: " + minJitter + "\n");
            textArea.append("Jitter Maximo: " + maxJitter + "\n");



            String msg = pacotesRecebidos + " " + bytesRecebidos + " " + timeStampPacotes[timeStampPacotes.length-1] + " " + jitterMedio + " " + minJitter + " " + maxJitter;
            saida.println(msg);
            saida.flush();

            //----------------------------------------------------------------------------------------------
            String testex = "Fim do programa";
            saida.println(testex);
            saida.flush();
            conexao.close();
            textArea.append(testex +"\n");
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}