import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.*;
import java.sql.Date;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.border.EmptyBorder;
import org.apache.commons.net.ntp.NTPUDPClient;
import org.apache.commons.net.ntp.TimeInfo;


public class Client extends JFrame{

    //Variaveis Interface
    private JPanel contentPane;
    private JTextField pOrigem;
    private JTextField pDestino;
    private JTextField tamMensagem;
    private JTextField ipDestino;
    private JTextField inputField;
    


    //UserInputs----------------------------------------------------
    static int uiTeste;
    static int uiTesteN;
    static int uiPortaOrigem;
    static int uiPortaDestino;
    static int uiTamanhoMensagem;
    static String uiIpDestinoString;
    static InetAddress uiIpDestino;

    String ipDestinoString;


    //UDP----------------------------------------------------------
    DatagramSocket clientSocketUdp;
    DatagramPacket pacoteUdp;
    //TCP----------------------------------------------------------
    Socket clientSocketTcp;
    InputStreamReader inputStreamReader;
    PrintWriter saida;
    BufferedReader buferredReader;
    String tcpMsg;

    //Painel de texto
    JScrollPane scrollPane;
    GroupLayout gl_contentPane;
    JTextArea textArea;
    JComboBox choiceOptions;
    
    //Sincroniza horario
    String ntpServer;
    NTPUDPClient timeClient;
    InetAddress inetAddress;
    TimeInfo timeInfo;
    //Cria o frame
    public Client() {

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        int winWidth = 450;
        int winHeight = 300;
        setBounds(100, 100, winWidth, winHeight);
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(contentPane);

        pOrigem = new JTextField();
        pOrigem.setText("Porta de Origem");
        pOrigem.setColumns(10);

        pDestino = new JTextField();
        pDestino.setText("Porta de Destino");
        pDestino.setColumns(10);

        tamMensagem = new JTextField();
        tamMensagem.setText("Tamanho da Mensagem (Bytes)");
        tamMensagem.setColumns(10);

        ipDestino = new JTextField();
        ipDestino.setText("IP de Destino");
        ipDestino.setColumns(10);

        inputField = new JTextField();
        inputField.setText("Valor da Opcao Escolhida");
        inputField.setBounds(74*(winWidth/160)-9, 5*(winHeight/24)+1, (winWidth/3)+40, 21);
        inputField.setForeground(Color.BLACK);
        inputField.setColumns(10);
        contentPane.add(inputField);

        scrollPane = new JScrollPane();
        gl_contentPane = new GroupLayout(contentPane);
        textArea = new JTextArea();
        scrollPane.setViewportView(textArea);
        textArea.setEditable(false);
        contentPane.setLayout(gl_contentPane);

        choiceOptions = new JComboBox();
        choiceOptions.setModel(new DefaultComboBoxModel(new String[] {"--- Selecione um Opcao ---", "Numero de Pacotes", "Total de Bytes", "Duracao do Teste"}));

        JButton initTest = new JButton("Iniciar Teste");
        initTest.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {



                if(choiceOptions.getSelectedItem().equals("--- Selecione um Opcao ---")) {
                    textArea.append("Uma das Opcoes deve estar selecionada!\n");
                }

                if(ipDestino.getText().trim().equals("") || pOrigem.getText().trim().equals("") || pDestino.getText().trim().equals("") || tamMensagem.getText().trim().equals("")) {
                    textArea.append("Preencha os campos obrigatorios!\n");
                } else {
                    uiPortaOrigem = Integer.parseInt(pOrigem.getText());
                    uiPortaDestino = Integer.parseInt(pDestino.getText());
                    uiTamanhoMensagem = Integer.parseInt(tamMensagem.getText());
                    uiTesteN = Integer.parseInt(inputField.getText());

                    try {//ip = localhost sempre
                        uiIpDestino = InetAddress.getByName("localhost");
                    } catch (UnknownHostException unknownHostException) {
                        unknownHostException.printStackTrace();
                    }

                    try {
                        clientSocketTcp = new Socket("localhost", 8888);
                        saida = new PrintWriter(clientSocketTcp.getOutputStream());

                        textArea.append("Conectado ao servidor " + "endereco" + ", na porta: 8888\n");

                        //--------------------------------------------------------------------------------------

                        clientSocketUdp = new DatagramSocket(uiPortaOrigem);

                        uiTeste = choiceOptions.getSelectedIndex();

                        tcpMsg = uiTeste + " " + uiTesteN + " " + uiTamanhoMensagem + " " + uiPortaDestino + " " + uiPortaOrigem + " " + uiIpDestino;
                        saida.println(tcpMsg);
                        saida.flush();

                        long startTime = 0;
                        ntpServer = "a.st1.ntp.br";
                        timeClient = new NTPUDPClient();
                        inetAddress = InetAddress.getByName(ntpServer);
                        timeInfo = timeClient.getTime(inetAddress);

                        
                        switch (uiTeste){
                            //switch que define qual teste vai ser feito
                            //- 1 = por numero de pacotes
                            //- 2 = por numero de bytes
                            //- 3 = por numero de milisegundos
                            case 1:
                                System.out.println("XXXXXXXXXXXXXXX");
                                //pegando o horario no servidor a.st1.ntp.br
                                startTime = timeInfo.getReturnTime();
                                for (int i = 0; i < uiTesteN; i++) {
                                    udpSend(clientSocketUdp, uiTamanhoMensagem, uiIpDestino, uiPortaDestino, i);
                                    System.out.println(1);
                                }

                                break;
                            case 2:
                                if (uiTesteN <=uiTamanhoMensagem){

                                    break;
                                }
                                int aux = uiTesteN/uiTamanhoMensagem;
                                if(uiTesteN % uiTamanhoMensagem != 0){
                                    aux++;
                                }

                                startTime = timeInfo.getReturnTime();
                                for (int i = 0; i < aux; i++) {
                                    udpSend(clientSocketUdp, uiTamanhoMensagem, uiIpDestino, uiPortaDestino, i);
                                    System.out.println(1);
                                }

                                System.out.println("Enviado " + aux * uiTamanhoMensagem + " Bytes");
                                break;
                            case 3:

                                int i = 0;
                                startTime = timeInfo.getReturnTime();
                                while(timeInfo.getReturnTime() <= startTime + uiTesteN){
                                    udpSend(clientSocketUdp, uiTamanhoMensagem, uiIpDestino, uiPortaDestino, i);
                                    System.out.println(1);
                                    i++;
                                }
                                System.out.println(i);
                                break;
                            default:
                                break;
                        }


                        TimeUnit.MILLISECONDS.sleep(1000);

                        tcpMsg = startTime + "";
                        saida.println(tcpMsg);
                        saida.flush();

                        //Get Results from server---------------------------------------------------------------------------------------
                        inputStreamReader = new InputStreamReader(clientSocketTcp.getInputStream());
                        buferredReader = new BufferedReader(inputStreamReader);
                        String resp = buferredReader.readLine();
                        String[] aux = resp.split(" ");
                        int pacotesRecebidos = Integer.parseInt(aux[0]);
                        int bytesRecebidos = Integer.parseInt(aux[1]);
                        long endTime = Long.parseLong(aux[2]);
                        double jitterMedio = Double.parseDouble(aux[3]);
                        int minJitter = Integer.parseInt(aux[4]);
                        int maxJitter = Integer.parseInt(aux[5]);
                        
                        //Print de resultados
                        
                        textArea.append("---------------- RESUMO -------------------\n" + 
                        				"Porta de Origem (UDP): " + uiPortaOrigem + "\n" +
                        				"Porta de Destino (UDP): "+ uiPortaDestino + "\n" +
                        				"Tamanho da mensagem (Bytes): " + uiTamanhoMensagem + "\n" +
                        				"IP de Destino: " + uiIpDestino + "\n");
                        				if(uiTeste == 1) {
                        					 textArea.append("Numero de Pacotes: " + uiTesteN +"\n");	
                        				}else if(uiTeste == 2) {
                        					textArea.append("Total de Bytes: " + uiTesteN +"\n");
                        				}else if(uiTeste == 3) {
                        					textArea.append("Duracao do Teste: " + uiTesteN +" ms\n");
                        				}
                        textArea.append("\n------------- RESULTADOS ----------------\n");
                        textArea.append("Total de Pacotes recebidos: " + pacotesRecebidos + "\n");
                        textArea.append("Total de Bytes recebidos: " + bytesRecebidos + "\n");
                        textArea.append("Tempo de Inicio: " + startTime + "\n" +
                        				"Tempo final: " + endTime + "\n" + 
                        				"Tempo total de Envio (milissegundos): " + (endTime-startTime) + "ms\n" +
                        				"Taxa de Transferencia: A calcular" + "\n" +
                        				"% de Perda de Pacotes: A calcular" + "\n");
                        textArea.append("Jitter Medio: " + jitterMedio + "\n" +
                        				"Jitter Minimo: " + minJitter + "\n" +
                        				"Jitter Maximo: " + maxJitter + "\n");
                        //-----------------------------------------------------------------------------------------


                        inputStreamReader = new InputStreamReader(clientSocketTcp.getInputStream());
                        buferredReader = new BufferedReader(inputStreamReader);
                        resp = buferredReader.readLine();
                        textArea.append(resp);

                        clientSocketTcp.close();

                    }catch (Exception e1){
                        e1.printStackTrace();
                    }

                }

            }
        });




        gl_contentPane.setHorizontalGroup(
                gl_contentPane.createParallelGroup(Alignment.LEADING)
                        .addGroup(gl_contentPane.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
                                        .addGroup(gl_contentPane.createSequentialGroup()
                                                .addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
                                                        .addGroup(gl_contentPane.createSequentialGroup()
                                                                .addGroup(gl_contentPane.createParallelGroup(Alignment.TRAILING)
                                                                        .addComponent(ipDestino, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                                                        .addComponent(pDestino, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                                                .addGap(18)
                                                                .addComponent(tamMensagem, GroupLayout.DEFAULT_SIZE, 197, Short.MAX_VALUE))
                                                        .addGroup(gl_contentPane.createSequentialGroup()
                                                                .addComponent(pOrigem, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                                                .addGap(18)
                                                                .addComponent(choiceOptions, 0, 197, Short.MAX_VALUE)))
                                                .addGap(18)
                                                .addComponent(initTest, GroupLayout.PREFERRED_SIZE, 76, GroupLayout.PREFERRED_SIZE))
                                        .addComponent(scrollPane, GroupLayout.PREFERRED_SIZE, 405, Short.MAX_VALUE))
                                .addGap(11))
        );
        gl_contentPane.setVerticalGroup(
                gl_contentPane.createParallelGroup(Alignment.LEADING)
                        .addGroup(gl_contentPane.createSequentialGroup()
                                .addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING, false)
                                        .addGroup(gl_contentPane.createSequentialGroup()
                                                .addGroup(gl_contentPane.createParallelGroup(Alignment.BASELINE)
                                                        .addComponent(tamMensagem, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(ipDestino, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                                .addGap(5)
                                                .addGroup(gl_contentPane.createParallelGroup(Alignment.BASELINE)
                                                        .addComponent(pOrigem, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(choiceOptions, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                                .addPreferredGap(ComponentPlacement.RELATED)
                                                .addComponent(pDestino, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                        .addComponent(initTest, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addGap(18)
                                .addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 157, Short.MAX_VALUE)
                                .addContainerGap())
        );

    }

    public static void udpSend(DatagramSocket clientSocketUdp, int tamanhoMsg, InetAddress ipDestino, int uiPortaDestino, int index) throws IOException {
        byte[] msg = new byte[tamanhoMsg];
        System.out.println(ipDestino + " " + uiPortaDestino);
        msg[0] = (byte)index;
        DatagramPacket pacoteUdp = new DatagramPacket(msg, msg.length, ipDestino, uiPortaDestino);
        clientSocketUdp.send(pacoteUdp);
    }

    public static void main(String[] args) throws IOException  {

        //Inicia a interface
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    Client frame = new Client();
                    frame.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        //-------------------------------------------------------------
//        System.out.println("Ip de destino");
//        uiIpDestinoString = in.nextLine();
//        uiIpDestino = uiIpDestinoString;
        //----------------------------------------------------------

    }
}