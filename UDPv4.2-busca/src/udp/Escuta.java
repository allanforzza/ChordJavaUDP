package udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import javax.swing.JOptionPane;

class Escuta extends Thread
{   
    No no;
    String ip,ipa, ips;
    public Escuta(No no)
    {
        this.no = no; //Passa o nó como referencia para permitir a alteração dentro da classe escuta      
    }        
    public void run()
    {
            try 
            {
                
                escutaPorta();//Função que escuta a rede.
            } 
            catch (IOException ex) 
            {
                System.out.println(ex.getMessage());                
            }           
    } 
    //Método que transforma um byte em inteiro.
    public static int byteArray4ToInt(byte[] b,int inicio) 
    {
            return ((((int)b[inicio]) << 24) | (((int)b[inicio+1]& 0xFF) << 16) | (((int)b[inicio+2]& 0xFF) << 8) | (((int)b[inicio+3]& 0xFF) ));                        
    }   
    
    //Escuta a porta da rede.
    void escutaPorta() throws IOException 
    {
        int portaserver = 12345;
        DatagramSocket servidorSocket = new DatagramSocket(portaserver);
        byte[] dadosRecebidos         = new byte[21];//Tamanho do pacote recebido. Colocamos tudo como 21 bytes. Poderíamos ter colocado cada um do tamanho necessário.
       
        while (true)
        {    
            // Declara o pacote a ser recebido.
            DatagramPacket pacoteRecebido = new DatagramPacket(dadosRecebidos, dadosRecebidos.length);
            servidorSocket.receive(pacoteRecebido);
            
            //Captura o IP do pacote recebido.
            InetAddress pkRecebido = pacoteRecebido.getAddress();
            String IPPacoteRecebido = pkRecebido.getHostAddress();

            int msg = (int)(dadosRecebidos[0]);//Pega o identificador da menssagem na posição 0.
            
            
            //De acordo com o identificador da mensagem que chegou, faz o tratamento: 0 join... 1 leave..., etc -- conforme protocolo.
            if (msg==0)//Join
            {
                 int idNovoNoRede   = byteArray4ToInt(dadosRecebidos,1);                 
                 no.RespostaJoin(IPPacoteRecebido,no.identificador, no.IPNo,no.IDantecessor,no.IPantecessor);             
                 no.IDantecessor    = idNovoNoRede;
                 no.IPantecessor    = IPPacoteRecebido;
            }
            
            if (msg==1)//Leave
            {
                 int idNoSaindoRede     = byteArray4ToInt(dadosRecebidos,1);
                 int idSucessorSaindo   = byteArray4ToInt(dadosRecebidos,5);
                 ips = retornaIP(dadosRecebidos,9);
                    
                 int idAntecessorSaindo = byteArray4ToInt(dadosRecebidos,13);
                 ipa = retornaIP(dadosRecebidos,17); 
               
                 no.Log.add("Recebido Leave "+IPPacoteRecebido);
                 if (idNoSaindoRede == no.IDantecessor)
                 {
                     no.IDantecessor = idAntecessorSaindo;
                     no.IPantecessor = ipa;
                     //no.Log.add("Enviou resposta Leave: "+ip);
                     no.RespostaLeave(IPPacoteRecebido);
                 }   
                 
                 if ((idNoSaindoRede == no.IDsucessor ) )
                 {
                     no.IDsucessor   = idSucessorSaindo;
                     no.IPsucessor = ips;
                     //no.Log.add("Enviou resposta Leave: "+ip);
                     no.RespostaLeave(IPPacoteRecebido);
                 }    
            }
            
            if (msg==2)//Lookup
            {
                no.Log.add("Recebido lookup "+IPPacoteRecebido);
                 //Captura os dados que vieram no pacote.
                 int idOrigem      = byteArray4ToInt(dadosRecebidos,1);
                 ip = retornaIP(dadosRecebidos,5);
                                  
                 int idProcurado = byteArray4ToInt(dadosRecebidos,9); 
                 
                 no.Log.add("IdProcurado "+idProcurado+" identificador: "+no.identificador +" antecessor: "+no.IDantecessor+" Sucessor: "+no.IDsucessor);
                 
                 
                 if ( (idProcurado == no.identificador) || // Responde caso alguém esteja me procurando
                      ((( no.identificador > idProcurado)&&(no.identificador > no.IDsucessor)&&(idProcurado < no.IDsucessor)) )||
                      ((no.identificador <  idProcurado)&&(no.IDsucessor > idProcurado)) || 
                      ((no.identificador <  idProcurado)&&(no.IDsucessor < idProcurado)&&(no.identificador > no.IDsucessor)) ||
                       (no.identificador == no.IDsucessor)) //Só tem 1 nó na rede.
                       
                 {
                     no.Log.add("Enviou resposta Lookup: "+ip);
                     no.RespostaLookup(ip,idProcurado,no.IDsucessor, no.IPsucessor);
                 }    
                 else //Caso contrário envia msg para o nó sucessor.
                 {
                     no.Log.add("Passou Sucessor: "+no.IPsucessor);
                     no.Lookup(no.IPsucessor,idOrigem,ip,idProcurado);
                 }
            }
    
            if (msg==3)//Update
            {
                no.Log.add("Recebido Update "+IPPacoteRecebido);
                 int idOrigem       = byteArray4ToInt(dadosRecebidos,1);
                 int idNovoSucessor = byteArray4ToInt(dadosRecebidos,5);
                 ip = retornaIP(dadosRecebidos,9);
                                   
                 
                 no.IDsucessor      = idNovoSucessor;
                 no.IPsucessor      = ip;
                 
                 no.RespostaUpdate(IPPacoteRecebido);
            }

            if (msg==64)//Resposta do Join
            {
                no.Log.add("Recebido Resposta Join "+IPPacoteRecebido);
                int idsucessor     = byteArray4ToInt(dadosRecebidos,1);
                ips = retornaIP(dadosRecebidos,5);
                   
                
                int   idAntecessor = byteArray4ToInt(dadosRecebidos,9);
                ipa = retornaIP(dadosRecebidos,13);
                  
                
                no.IDsucessor      = idsucessor;
                no.IPsucessor      = ips;
                
                no.IDantecessor    = idAntecessor;
                no.IPantecessor    = ipa;
                no.Update(ipa, no.identificador, no.IPNo);
                
            }

            if (msg==65)//Resposta do Leave
            {
                
                // JOptionPane.showMessageDialog(null, "Resposta LEAVE: Recebido: "+DadoRecebido.trim()+" Id  "+no.identificador, "INFORMATION_MESSAGE", JOptionPane.INFORMATION_MESSAGE);                     
            } 

            if (msg==66) //Resposta do Lookup
            {
                 
                 
                 no.Log.add("Recebido Resposta Lookup "+IPPacoteRecebido);
                 //Captura os dados que vieram no pacote.
                 int idProcurado   = byteArray4ToInt(dadosRecebidos,1);
                 int idSucessor    = byteArray4ToInt(dadosRecebidos,5);
                 ip = retornaIP(dadosRecebidos,9);
                 //no.Log.add("ID PROCURADO É: "+idProcurado+" ID SUCESSOR É: "+idSucessor);
                 // Depois de receber a resposta do Lookup, faz um Join na Rede
                 if(!no.checaRede())
                     no.join(ip, no.identificador);
                 else
                     no.Log.add("ID PROCURADO É: "+idProcurado+" ID SUCESSOR É: "+idSucessor);
                                 
                 
                 
            } 

            if (msg==67)//Resposta do Update
            {
                no.Log.add("Recebido Resposta Update "+IPPacoteRecebido);
                 int idOrigem      = byteArray4ToInt(dadosRecebidos,1);                 
            } 
            
        }
    }    
    
    public String retornaIP(byte[] enderecoIp,int posicao)
    {
        byte[] aux = new byte[4];
        aux[0]=enderecoIp[posicao];
        aux[1]=enderecoIp[posicao+1];
        aux[2]=enderecoIp[posicao+2];
        aux[3]=enderecoIp[posicao+3];
        

        return enderecoIpByteToString(aux);  
    }
    public static String enderecoIpByteToString(byte[] enderecoIp)
    {
		int i = 4;
        String ipAddress = new String();
        for (byte raw : enderecoIp)
        {
            ipAddress += (raw & 0xFF);
            if (--i > 0)
            {
                ipAddress += ".";
            }
        }
        
        return ipAddress;
	}
    
    
}

