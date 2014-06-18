package udp;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;


public class UDP   extends JFrame implements ActionListener
{   
    private JButton    BLeavie, BEntrar;
    private No         noPrincipal;
    private JTextArea  LInfo, LLog;
    private Atualiza   atualiza; //thread
    JScrollPane barra ;
    
    //construtor da interface
    public UDP() throws SocketException, IOException, UnknownHostException, NoSuchAlgorithmException
    {
        //cria o objeto principal nó, que será utilizado nesta interface.
        noPrincipal = new No();      

               
        //Cria o botão de entrar na rede.
        BEntrar = new JButton("Entrar na Rede");
        BEntrar.setEnabled(true);
        BEntrar.addActionListener(this); 
        BEntrar.setBounds(10, 10, 200, 60);
        
        //Cria o botão de sair da rede.
        BLeavie = new JButton("Sair");
        BLeavie.setEnabled(true);
        BLeavie.addActionListener(this);
        BLeavie.setBounds(10, 80, 200, 60);
        
        //Cria o campo log.

        
        LLog = new JTextArea (" ");   
        LLog.setEditable(false);//fala que o texto não é editável  
        LLog.setFont(new Font("Tahoma", Font.BOLD, 12));//troca a fonte do texto
        LLog.setLineWrap(true);
        LLog.setEditable(true);
        LLog.setWrapStyleWord(true); 
        
        barra = new JScrollPane(LLog);
        barra.setBounds(10, 150, 510, 300);//posicao e tamanho
        barra.setVerticalScrollBarPolicy(barra.VERTICAL_SCROLLBAR_AS_NEEDED);
        barra.setHorizontalScrollBarPolicy(barra.HORIZONTAL_SCROLLBAR_NEVER); 
       

        //Cria o campo informativo.
        LInfo = new JTextArea (" Info: ");
        LInfo.setBounds(220, 10, 300, 130);//posicao e tamanho
        LInfo.setEditable(false);//fala que o texto não é editável  
        LInfo.setFont(new Font("Tahoma", Font.BOLD, 12));//troca a fonte do texto
        
        this.setTitle("No UDP");//Seta o título da tela principal.
        this.setSize(550,500);//Seta o tamanho.
        this.setLocation(200,200);//seta a posicao em relacao ao desktop
        this.getContentPane().setBackground(new Color(180,180,180));//seta a cor do form principal
        this.getContentPane().setLayout(null);//seta que não tem gerenciador de layout no formulario principal
        
        this.getContentPane().add(this.BLeavie); //adiciona o botao de sair ao form principal
        this.getContentPane().add(this.BEntrar); //adiciona o botao de entrar ao form principal
        this.getContentPane().add(this.LInfo);  //adiciona o componente de visualizacao ao componente principal
        //this.getContentPane().add(this.LLog); 
        this.getContentPane().add(this.barra); 
        Thread clockThread = new Thread(new Atualiza(this), "Clock thread");//cria a thread...
        clockThread.setDaemon(true);//esqueci... i carai
        clockThread.start(); //starta a thread
    }       
    
    public void actionPerformed(ActionEvent e)//toda vez que uma ação desse tipo executar esse metodo executa...
    {
        if (e.getSource()==BEntrar)/// se o originador da ação for o botão de entrar ele faz isso.
        {
            try 
            {
                String IP = JOptionPane.showInputDialog(null, "Informe o IP a se Conectar", "IP", JOptionPane.INFORMATION_MESSAGE);                if ((!IP.trim().equals("")) && (!IP.trim().equals(noPrincipal.IPNo))) //testa se é vazio ou diferente o proprio ip
                if (!IP.trim().equals("")) //Testa se foi digitado vazio.
                {  
                   //if (InetAddress.getByName(IP).isReachable(5000)) //Testa se o IP está na Rede.
                   // {                                           
                        noPrincipal.entrarNaRede(IP); //Passa o IP que o nó deseja se conectar na rede.
                        BEntrar.setEnabled(false);    //Desabilita o botão de entrar.
                        
                   //}
                   // else
                   //   JOptionPane.showMessageDialog(null, "IP Informado não foi encontrado na Rede");
                }
                else
                   JOptionPane.showMessageDialog(null, "Digite o IP da Máquina que deseja se conectar", "INFORMATION_MESSAGE", JOptionPane.INFORMATION_MESSAGE);    
            } 
            catch (SocketException ex){} catch (IOException ex){}
        }    

        if (e.getSource()==BLeavie)//se o originador da ação for o botão
        {

                try
                {     
                 
                    BLeavie.setEnabled(false);    //Desabilita o botão de sair
                    noPrincipal.Leave();//Executa o método Leave da classe no principal.
                    
                            }
                catch (Exception c) {} //Não faz nada em caso erro no Leave                
                System.exit(0); //Fecha a aplicação
                
        }          
    }  
    
    public static void main(String[] args) throws SocketException, IOException, UnknownHostException, NoSuchAlgorithmException 
    {
        //cria a frame...janela
        JFrame janela = new UDP();//inicia a interface 
        janela.setUndecorated(true);
        janela.getRootPane().setWindowDecorationStyle(JRootPane.FRAME);
        janela.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        janela.setVisible(true);
    }
    public void atualizar() //Somente exibe as informacões do nó principal
    {
        BEntrar.setEnabled(noPrincipal.IPNo == noPrincipal.IPsucessor);
        
        LInfo.setText(" Identificador deste Nó: "          +noPrincipal.identificador+   "\n" + 
                      " IP deste Nó: "                     +noPrincipal.IPNo         +   "\n" +"\n"+
                      " Identificador do Nó Antecessor: "  +noPrincipal.IDantecessor +   "\n" +
                      " IP do Nó Antecessor: "             +noPrincipal.IPantecessor +   "\n" + "\n"+
                      " Identificador do Nó Sucessor: "    +noPrincipal.IDsucessor   +   "\n" +
                      " IP do Nó Sucessor: "               +noPrincipal.IPsucessor   +   "\n" );
        LLog.setText(noPrincipal.getLog()+LLog.getText());
                
    }        
}
class Atualiza implements Runnable //É a thread que executa para ficar atualizando os dados dentro da interface.
{   
    UDP ref;
    public Atualiza(UDP ref)
    {
        this.ref = ref;        
    }    
    public void run()
    {
            while (true)         
            {
                try
                {   
					// Atualiza a tela da aplicação a cada 2 segundos
                    ref.atualizar();
                    Thread.sleep(2000);
                }
                catch (Exception x){}
            }    
    } 
    

}