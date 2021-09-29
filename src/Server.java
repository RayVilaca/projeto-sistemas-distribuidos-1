import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Server {
	
	private static boolean lock;
	
	private static void crivo(boolean[] numbers) {
		
		for(int i = 2; i <= 1000000; ++i) {
			if(numbers[i] == false) {	
				for(int j = 2*i; j <= 1000000; j += i) {
					numbers[j] = true;
				}
			}
		}
	}
	
	public static void main(String[] args) {
		
		ServerSocket server = null;
		Socket connection = null;
		ClientHandler client = null;
		
		BlockingQueue<Pair<Integer, Integer>> requisicoes = new LinkedBlockingQueue<>();
		Balancer balancer = new Balancer(requisicoes);
		Thread thread_balancer = new Thread(balancer);
		thread_balancer.start();
		
		try  {
			server = new ServerSocket(5555);
			server.setReuseAddress(true);
			System.out.println("Servidor ouvindo na porta 5555");
			
			for(int i = 1; ; i++) {
				connection = server.accept();
				System.out.println("Cliente " + i + " conectado:" +
				connection.getInetAddress().getHostAddress());
				
				client = new ClientHandler(i, connection, requisicoes);
				new Thread(client).start();
						
			}
			
		} catch (Exception e) {
			System.out.println("Erro:" + e.getMessage());
		} finally {
			try {
				server.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}	
			
	}
	
	public static class ServerHandler implements Runnable {
		
		private int id;
		private Equalizer equalizer;
		BlockingQueue<Pair<Integer, Integer>> requisicoes;

		public ServerHandler(int id, BlockingQueue<Pair<Integer, Integer>> requisicoes) {
			this.id = id;
			this.requisicoes = requisicoes;
		}

		public void setEqualizer(Equalizer equalizer) {
			this.equalizer = equalizer;
		}
		
		public void equalizarArq(String msg) {
			Arq.write(msg, "arquivo"+id+".txt");
		}

		public void process(int client, int number) {
			
			boolean[] numbers = new boolean[1000006];
			crivo(numbers);
			
			Thread t;
			Random random = new Random();
			
			while(lock == true) {		
				System.out.println("Servidor " + id + " está aguardando para processar"
						+ " a requisicao do cliente " + client + " de numero " + number);
				try {
					Thread.sleep(random.nextInt(((6 - 0)+ 1) + 0) * 100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			} 
			
			System.out.println("Servidor " + id + " esta processando a "
					+ "requisicao " + number + " do cliente " + client);
			if(number == -1) {
				Arq.read("arquivo" + id + ".txt");
			} else {
				lock = true;
				
				String msg = numbers[number]? " não é primo":" é primo";
				
				System.out.println("Resultado do processamento: "+ number + msg);
				
				Arq.write(number + msg, "arquivo" + id + ".txt");
				
				equalizer.setServer(id);
				equalizer.setMsg(number + msg);
				t = new Thread(equalizer);
				t.start();
				
				try {
					t.join();
					Thread.sleep(random.nextInt(((3 - 0)+ 1) + 0) * 100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				lock = false;
				System.out.println("Acesso da requisicao " + number + " ao recurso liberado");
				
				try {
					Thread.sleep(random.nextInt(((4 - 3)+ 1) + 3) * 100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
		}
		
		@Override
		public void run() {
			
			Pair<Integer, Integer> req;
			Random random = new Random();
			
			int number, client;
			
			File file = new File("arquivo" + id + ".txt");
			
			if(file.exists()) {
				file.delete();
			}
			
			try {
				file.createNewFile();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			
			while(true) {
				
				try {
					req = requisicoes.take();
					client = req.first;
					number = req.second;
					
					while(lock == true) {		
						System.out.println("Servidor " + id + " está aguardando para processar"
								+ " a requisicao do cliente " + client + " de numero " + number);
						try {
							Thread.sleep(random.nextInt(((6 - 0)+ 1) + 0) * 100);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					} 
					
					this.process(client, number);
					System.out.println();
					
				} catch (InterruptedException e) {
					System.out.println("Servidor " + id + " foi interrompido");
				}
			}
			
		}
		
	}
	
	public static class Equalizer implements Runnable {
		
		private int server;
		private String msg;
		private ServerHandler server1, server2, server3;

		public Equalizer(ServerHandler server1, ServerHandler server2, ServerHandler server3) {
			this.server1 = server1;
			this.server2 = server2;
			this.server3 = server3;
		}
		
		public void setServer(int server) {
			this.server = server;
		}

		public void setMsg(String msg) {
			this.msg = msg;
		}
		
		@Override
		public void run() {
			
			if(server != 1) {server1.equalizarArq(msg);}
			if(server != 2) {server2.equalizarArq(msg);}
			if(server != 3) {server3.equalizarArq(msg);}
			System.out.println("Equalização concluída");
		}
		
	}
	
	public static class Balancer implements Runnable {
		
		private BlockingQueue<Pair<Integer, Integer>> requisicoes;

		public Balancer(BlockingQueue<Pair<Integer, Integer>> requisicoes) {
			this.requisicoes = requisicoes;
		}

		@Override
		public void run() {
			
			int op;
			Random random = new Random();
			
			Pair<Integer, Integer> req;
			
			BlockingQueue<Pair<Integer, Integer>> bq1 = new LinkedBlockingQueue<>();
			BlockingQueue<Pair<Integer, Integer>> bq2 = new LinkedBlockingQueue<>();
			BlockingQueue<Pair<Integer, Integer>> bq3 = new LinkedBlockingQueue<>();
			
			ServerHandler server1 = new ServerHandler(1, bq1);
			ServerHandler server2 = new ServerHandler(2, bq2);
			ServerHandler server3 = new ServerHandler(3, bq3);
			
			Equalizer equalizer = new Equalizer(server1, server2, server3);
			
			server1.setEqualizer(equalizer);
			server2.setEqualizer(equalizer);
			server3.setEqualizer(equalizer);
			
			Thread thread_server1 = new Thread(server1);
			Thread thread_server2 = new Thread(server2);
			Thread thread_server3 = new Thread(server3);
			
			thread_server1.start();
			thread_server2.start();
			thread_server3.start();
			
			while(true) {
				try {
					req = requisicoes.take();
					System.out.println();
					System.out.println("Balanceador desbloqueado");
					
					op = random.nextInt(((3 - 1) + 1) + 1);
					
					if(op == 1) {
						bq1.put(req);
						System.out.println("Requisicao " + req.second + " do cliente " + req.first + " direcionada ao Servidor 1");
						System.out.println();
					} else if(op == 2) {
						bq2.put(req);
						System.out.println("Requisicao " + req.second + " do cliente " + req.first + " direcionada ao Servidor 2");
						System.out.println();
					} else {
						bq3.put(req);
						System.out.println("Requisicao " + req.second + " do cliente " + req.first + " direcionada ao Servidor 3");
						System.out.println();
					}
				} catch (InterruptedException e) {
					System.out.println("Balanceador foi interrompido");
				}
			
			}
		
		}
	}
	
	public static class ClientHandler implements Runnable {
		
		private final int id;
		private final Socket client;
		BlockingQueue<Pair<Integer, Integer>> requisicoes;
		
		public ClientHandler(int id, Socket client, BlockingQueue<Pair<Integer, Integer>> requisicoes) {
			this.id = id;
			this.client = client;
			this.requisicoes = requisicoes;
		}

		@Override
		public void run() {
			
			int	number;
			ObjectInputStream input = null;
			Pair<Integer, Integer> p;
			
			try {
				
				while(true) {
					input = new ObjectInputStream(client.getInputStream());
					
					number = input.readInt();
					p = Pair.of(id, number);
					requisicoes.put(p);
					
				}
				
				
			} catch (Exception e) {
				System.out.println("Erro:" + e.getMessage());
			} finally {
				try {
					input.close();
					client.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
		}
		
	}

}
