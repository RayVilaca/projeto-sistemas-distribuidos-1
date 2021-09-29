import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class Arq {
	
	public static void read(String path) {
		System.out.println("Conteúdo do arquivo " + path);
		try (BufferedReader br = new BufferedReader(new FileReader(path))) {
			String line = br.readLine();
			while (line != null) {
				System.out.println("->"+line);
				line = br.readLine();
			}
		} catch (IOException e) {
			System.out.println("Error: " + e.getMessage());
		} 
	}
	
	public static void write(String msg, String path) {
		
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(path, true))) {
			bw.write(msg);
			bw.newLine();
		
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
