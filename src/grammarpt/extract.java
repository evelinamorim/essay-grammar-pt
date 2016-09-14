package grammarpt;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Locale;

import org.cogroo.analyzer.Analyzer;
import org.cogroo.analyzer.ComponentFactory;
import org.xml.sax.SAXException;


public class extract {
	   public static void main(String[] args) throws SAXException, IOException {
		   
		   String dir_data = "/Users/evelin.amorim/Documents/UFMG/aes/data/"; // args[0];
		   String fts_file =  "/Users/evelin.amorim/Documents/UFMG/aes/baseftrs.csv" ;  // args[1];
		   ReadData rd = new ReadData();
	       
		   long startTime = System.currentTimeMillis();
		   
	       List<Essay> essayList = rd.read(dir_data);
	       
	       long endTime = System.currentTimeMillis();
	       long milliseconds =  (endTime - startTime);
	       int seconds = (int) (milliseconds / 1000) % 60;
	       System.out.println("Reading data took " + seconds + " seconds");
	       
		   ComponentFactory factory = ComponentFactory.create(new Locale("pt", "BR"));
		   Analyzer cogroo = factory.createPipe();
		   
		   FileWriter fw = new FileWriter(fts_file);
		   BufferedWriter bw = new BufferedWriter(fw);
		   PrintWriter out = new PrintWriter(bw);
		   
		   int i = 0;
		
		   
		   // pq o tempo de extracao de features vai aumentando até voltar a diminuir?
		   // o que esta levando tanto tempo?
		   for (Essay e: essayList){
			   startTime = System.currentTimeMillis();
		       if (i == 0){
		    	   e.writeFeatures( out, true);   
		       }else{
		    	   e.writeFeatures(out, false);
		       }
		       endTime = System.currentTimeMillis();
		       milliseconds =  (endTime - startTime);
		       seconds = (int) (milliseconds / 1000) % 60;
		       System.out.println( i + " Extracting/writing data took " + seconds + " seconds");
		       System.out.println(e.getfileName());
		       i++;
		   }

	   }
}
