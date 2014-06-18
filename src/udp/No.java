package udp;

import java.io.IOException;
import java.math.BigInteger;
import java.net.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import javax.swing.JOptionPane;

public class No 
{
    String     IPsucessor;
    String     IPantecessor;
    
    ArrayList <String> Log = new ArrayList();
    private    int idLog=0;
    
    String     IPNo;//IP Próprio nó
    
    int        IDsucessor;
    int        IDantecessor;
    
    int        identificador;
    
    
    Escuta     escut; // Funcionalidade da thread
    int        porta = 12345;
    
    public No() throws SocketException, IOException, UnknownHostException, NoSuchAlgorithmException
    {
        //Gera identificador em cima do IP
        IPNo                     = pegaIpLocal();
        identificador            = md5(IPNo) & 0xffff;
        //identificador            = geraHashMd5();// Este método está apenas retornando o último octeto (em decimal) do IP.
        IDsucessor               = identificador ; // Na criação da rede, os IDs do Sucessor e Antecessor são iguais ao ID do Nó atual
        IDantecessor             = identificador ;
        IPantecessor             = IPNo;
        IPsucessor               = IPNo;
               
        //Inicializa a thread que escuta os outros nós.
        //Note que é passado o No como parâmetro this. Dessa forma, na thread conseguimos alterar o NO
        escut = new Escuta(this);
        escut.start();
        
    }      
    
    void join(String ip,int IdNovoNo) throws SocketException, IOException
    {    
        Log.add("Enviado Join "+ip);
        DatagramSocket clienteSocket = new DatagramSocket();
        byte[] enviaDados            = new byte[21];
        
        //IDENTIFICADOR DA MSG
        enviaDados[0]  = (byte)0;
        
        //IDENTIFICADOR DO NOVO NÓ DA REDE
        IntTobyteArray4(enviaDados,IdNovoNo,1);

        // Cria pacote com o dado, endereço IP do servidor e sua porta e ENVIA AO SUCESSOR
        InetAddress enderecoIP = InetAddress.getByName(ip);
        DatagramPacket enviaPacote = new DatagramPacket(enviaDados, enviaDados.length, enderecoIP, porta);            
        clienteSocket.send(enviaPacote);
    }
    
    void Leave() throws SocketException, IOException
    {   
        Log.add("Saindo da Rede");
        if (IPNo != IPsucessor)//Testa se o nó se conectou em alguma rede.
        {    
            DatagramSocket clienteSocket = new DatagramSocket();
            byte[] enviaDados = new byte[21];

            //IDENTIFICADOR DA MSG
            enviaDados[0]  = (byte)1;

            //IDENTIFICADOR DO NO SAINDO DA REDE
            IntTobyteArray4(enviaDados,identificador,1);

            //ID DO SUCESSOR DO NO SAINDO DA REDE
            IntTobyteArray4(enviaDados,IDsucessor,5);

            //IP DO SUCESSOR DO NO SAINDO DA REDE
            adicionaIPArray(enviaDados,9,IPsucessor);

            //ID DO ANTECESSOR DO NO SAINDO DA REDE
            IntTobyteArray4(enviaDados,IDantecessor,13);

            //IP DO ANTECESSOR DO NO SAINDO DA REDE
            adicionaIPArray(enviaDados,17,IPantecessor);

            // Cria pacote com o dado, endereco IP do servidor e sua porta E ENVIA AO SUCESSOR
            InetAddress enderecoIP = InetAddress.getByName(IPsucessor);
            DatagramPacket enviaPacote = new DatagramPacket(enviaDados, enviaDados.length, enderecoIP, porta);            
            clienteSocket.send(enviaPacote);

            //ENVIA AO SUCESSOR
            enderecoIP = InetAddress.getByName(IPantecessor);
            enviaPacote = new DatagramPacket(enviaDados, enviaDados.length, enderecoIP, porta);            
            clienteSocket.send(enviaPacote);
        }     
    }        
    void entrarNaRede(String IP) throws UnknownHostException, SocketException, IOException
    {
         // Método para criar e entrar na Rede.
	 
        Lookup(IP,identificador,IPNo,identificador);
        Log.add("Entrando na Rede pelo IP: "+IP);
    }        
    
    void Lookup(String IP,int idOrigemProcura, String ipOrigemProcura, int idProcurado) throws UnknownHostException, SocketException, IOException
    {   
        Log.add("Enviado lookup "+IP);
        InetAddress enderecoIP = InetAddress.getByName(IP);
        DatagramSocket clienteSocket = new DatagramSocket();
        byte[] enviaDados = new byte[21];
        //IDENTIFICADOR DA MSG
        enviaDados[0]  = (byte)2;

        //IDENTIFICADOR 
        IntTobyteArray4(enviaDados,idOrigemProcura,1);

        //PEGA O IP DA ORIGEM DA PROCURA -- O IPLOCAL
        adicionaIPArray(enviaDados,5,ipOrigemProcura);

        //IDENTIFICADOR PROCURADO
        IntTobyteArray4(enviaDados,idProcurado,9);

        // Cria pacote com o dado, endereço IP do servidor e sua porta.
        DatagramPacket enviaPacote = new DatagramPacket(enviaDados, enviaDados.length, enderecoIP, porta);            
        // Envia o pacote.
        clienteSocket.send(enviaPacote);
    }    
              
    void Update(String ip,int idNovoSucessor, String IPNovoSucessor) throws SocketException, UnknownHostException, IOException
    {
        Log.add("Enviado UPDATE "+ip);
        DatagramSocket clienteSocket = new DatagramSocket();
        byte[] enviaDados = new byte[21];
        
        //IDENTIFICADOR DA MSG
        enviaDados[0]  = (byte)3;
        
        //IDENTIFICADOR DA ORIGEM
        IntTobyteArray4(enviaDados,identificador,1);
        
        //ID DO NOVO SUCESSOR
        IntTobyteArray4(enviaDados,idNovoSucessor,5);
        
        //IP DO NOVO SUCESSOR
        adicionaIPArray(enviaDados,9,IPNovoSucessor);

        // Cria pacote com o dado, endereço IP do servidor e sua porta e ENVIA AO SUCESSOR
        InetAddress enderecoIP = InetAddress.getByName(ip);
        DatagramPacket enviaPacote = new DatagramPacket(enviaDados, enviaDados.length, enderecoIP, porta);            
        clienteSocket.send(enviaPacote);
    }        
    
    void RespostaJoin(String ip,int IdSucessorNovo, String IPSucessorNovo,int IdAntecessorNovo, String IPAntecessorNovo) throws SocketException, UnknownHostException, IOException
    {
        Log.add("Enviado Resposta do JOIN "+ip);
        DatagramSocket clienteSocket = new DatagramSocket();
        byte[] enviaDados = new byte[21];
        
        //IDENTIFICADOR DA MSG
        enviaDados[0]  = (byte)64;        
       
        //ID DO SUCESSOR DO NO SAINDO DA REDE
        IntTobyteArray4(enviaDados,IdSucessorNovo,1);
        
        //IP DO SUCESSOR DO NO SAINDO DA REDE
        adicionaIPArray(enviaDados,5,IPSucessorNovo);
        
        //ID DO ANTECESSOR DO NO SAINDO DA REDE
        IntTobyteArray4(enviaDados,IdAntecessorNovo,9);
        
        //IP DO ANTECESSOR DO NO SAINDO DA REDE
        adicionaIPArray(enviaDados,13,IPAntecessorNovo);

        // Envia Pacote
        InetAddress enderecoIP = InetAddress.getByName(ip);
        DatagramPacket enviaPacote = new DatagramPacket(enviaDados, enviaDados.length, enderecoIP, porta);            
        clienteSocket.send(enviaPacote);        
    }        
    void RespostaLeave(String ip) throws SocketException, UnknownHostException, IOException
    {
        Log.add("Enviado Resposta do LEAVE "+ip);
        DatagramSocket clienteSocket = new DatagramSocket();
        byte[] enviaDados = new byte[21];
        
        //IDENTIFICADOR DA MSG
        enviaDados[0]  = (byte)65;
        
        //IDENTIFICADOR DO NO SAINDO DA REDE
        IntTobyteArray4(enviaDados,identificador,1);        
        
        // Envia Pacote
        InetAddress enderecoIP = InetAddress.getByName(ip);
        DatagramPacket enviaPacote = new DatagramPacket(enviaDados, enviaDados.length, enderecoIP, porta);            
        clienteSocket.send(enviaPacote);        
    }   
    
    void RespostaLookup(String ip,int IdProcurado, int IdSucessorProcurado,String IPSucessorProcurado) throws SocketException, UnknownHostException, IOException
    {
        Log.add("Enviado Resposta Lookup "+ip);
        DatagramSocket clienteSocket = new DatagramSocket();
        byte[] enviaDados = new byte[21];
        
        //IDENTIFICADOR DA MSG
        enviaDados[0]  = (byte)66;
        
        IntTobyteArray4(enviaDados,IdProcurado,1);        

        //ID DO SUCESSOR PROCURADO
        IntTobyteArray4(enviaDados,IdSucessorProcurado,5);
        
        //IP DO SUCESSOR PROCURADO
        adicionaIPArray(enviaDados,9,IPSucessorProcurado);
        
        // Envia Pacote
        InetAddress enderecoIP = InetAddress.getByName(ip);
        DatagramPacket enviaPacote = new DatagramPacket(enviaDados, enviaDados.length, enderecoIP, porta);            
        clienteSocket.send(enviaPacote);        
    }   
    
     void RespostaUpdate(String ip) throws SocketException, UnknownHostException, IOException
    {
        Log.add("Enviado Resposta Update "+ip);
        DatagramSocket clienteSocket = new DatagramSocket();
        byte[] enviaDados = new byte[21];
        
        //IDENTIFICADOR DA MSG
        enviaDados[0]  = (byte)67;
        
        //IDENTIFICADOR DO NO SAINDO DA REDE
        IntTobyteArray4(enviaDados,identificador,1);        

        // Envia Pacote
        InetAddress enderecoIP = InetAddress.getByName(ip);
        DatagramPacket enviaPacote = new DatagramPacket(enviaDados, enviaDados.length, enderecoIP, porta);            
        clienteSocket.send(enviaPacote);        
    }         
        
    //--------------------------------------------------------------------------
    //METODOS AUXILIARES...
    //--------------------------------------------------------------------------
    //deve gerar o inteiro a partir do IP da maquina;;;
    int geraHashMd5() throws UnknownHostException, NoSuchAlgorithmException
    {
        InetAddress addr = InetAddress.getLocalHost();
        String ip = addr.getHostAddress();
        String[] ipParte;
        short ip4;
        ipParte = ip.split("\\.");
        ip4 = Short.valueOf(ipParte[3]);        
        
        return ip4;       
    }
    
    //FUNCAO COLOCA O INTEIRO DE 4 BYTES DENTRO DO VETOR A PARTIR DA POSICAO INICIO INDICADA...
    public void IntTobyteArray4(byte[] b,int x,int inicio) 
    {
        b[inicio+3]   = (byte)  x;  
        b[inicio+2]   = (byte) (x >> 8);//deslocamento dos bytes  
        b[inicio+1]   = (byte) (x >> 16);  
        b[inicio]     = (byte) (x >> 24);   
    }
  
    //PEGA O IP LOCAL E COLOCA EM UMA POSICAO DENTRO DO VETOR DE BYTES
    public static byte[] enderecoIpStringToByte(String enderecoIp){
		InetAddress inetAddress = null;
		try {
			inetAddress = InetAddress.getByName(enderecoIp);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		return inetAddress.getAddress();
	}
    
    public void adicionaIPArray(byte[] b,int inicio,String ip) throws UnknownHostException
    {
        byte[] ipt = enderecoIpStringToByte(ip);
        //String[] ipParte;
        //short ip1,ip2,ip3,ip4;
        //ipParte = ip.split("\\.");//Gera uma lista com os IPs a partir do "."
        
        //ip1 = Short.valueOf(ipParte[0]);
        //ip2 = Short.valueOf(ipParte[1]);
        //ip3 = Short.valueOf(ipParte[2]);
        //ip4 = Short.valueOf(ipParte[3]);        
                
        b[inicio  ] = ipt[0];//TRATAMENTO DE TIPOS COM TAMANHO DIFERENTE...         
        b[inicio+1] = ipt[1];        
        b[inicio+2] = ipt[2];        
        b[inicio+3] = ipt[3];
    }

    //Pega o IP Local
    public String pegaIpLocal() throws UnknownHostException
    {
        InetAddress enderecoIpHost = null;

        List<Inet4Address> interfacesIpv4 = new ArrayList<Inet4Address>();
        Enumeration listaInterfaces = null;
        try {
                listaInterfaces = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException s) {
                System.err.println("*ERRO: Não foi possível obter as interfaces de rede. Por favor, contacte o desenvovedor.");
                s.printStackTrace();
                System.exit(0);
        }
        while(listaInterfaces.hasMoreElements()){
            NetworkInterface ni = (NetworkInterface)listaInterfaces.nextElement();
            Enumeration ee = ni.getInetAddresses();
            while(ee.hasMoreElements()) {
                try {
                        Inet4Address ia = (Inet4Address)ee.nextElement();
                        if (!ia.isLoopbackAddress())
                                interfacesIpv4.add(ia);
                }
                catch (Exception e){}
            }
        }

        if (interfacesIpv4.size() > 1)
        {
                ArrayList <String> Ips = new ArrayList();
                //System.out.println("*ATENÇÃO: Mais de uma interface de rede foi detectada:");
                int i = 1;
                for (Inet4Address inet4Address : interfacesIpv4) {
                        Ips.add(inet4Address.getHostAddress());
                        //System.out.println(i + " - " + inet4Address.getHostAddress());
                        i++;
                }
                 int resposta = JOptionPane.showOptionDialog(null, "Selecione Qual IP", "Seleção de IP", 0, JOptionPane.INFORMATION_MESSAGE, null, Ips.toArray(),0 );
                    //JOptionPane.showMessageDialog(null,"Retorno: "+Ips.get(resposta));
                 //return Ips.get(resposta).substring(0, Ips.get(resposta).length());
                 return Ips.get(resposta).toString(); // IGOR: Adicionei o toString
        }
        else 
        {         
                enderecoIpHost = interfacesIpv4.get(0);
                //int tam = enderecoIpHost.toString().length();
                //System.out.println(enderecoIpHost.toString().substring(1, tam));
                return enderecoIpHost.getHostAddress(); 
        }
}
       
    
    
	// Gera hash md5 dado com entrada uma string, que será o IP
    public static int md5(String item) throws NoSuchAlgorithmException  
    {     
        MessageDigest md = MessageDigest.getInstance("MD5");  
        BigInteger hash = new BigInteger(1, md.digest(item.getBytes()));  
        //return  hash.intValue();
        return Integer.parseInt(hash.toString().substring(0,4));
    }    
    public String getLog()
    {
        String aux = ""; 
        for (int i = idLog; i < Log.size();i++)
        {
            aux = aux +Log.get(i)+"\n";
            idLog++;
        } 
        return aux;
        
    }        
}





