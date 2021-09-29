import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Random;

public class Client {

	public static void main(String[] args) throws IOException {
		
		Socket client = null;
		ObjectOutputStream output = null;
		
		int op, number;
		Random random = new Random();

		try {
			
			client = new Socket("localhost", 5555);
			
			while(true) {
				output = new ObjectOutputStream(client.getOutputStream());
				
				op = random.nextInt((2 - 1) + 1) + 1;
				
				if(op == 1) {
					number = random.nextInt(((1000000- 2)+ 1) + 2);
				} else {
					number = -1;
				}
				
				System.out.println(number);
				
				output.writeInt(number);
				output.flush();
	
				Thread.sleep(300);			
			}
			
		} catch (Exception e) {
			System.out.println("Erro:" + e.getMessage());
		} finally {
			output.close();
			client.close();
		}
	}

}
