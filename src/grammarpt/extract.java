package grammarpt;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import org.cogroo.analyzer.Analyzer;
import org.cogroo.analyzer.ComponentFactory;
import org.xml.sax.SAXException;


public class extract {
	   public static void main(String[] args) throws SAXException, IOException {
		   ReadData rd = new ReadData();
	       
	       List<Essay> essayList = rd.read("/Users/evelin.amorim/Documents/UFMG/aes/data/");
	       
		   ComponentFactory factory = ComponentFactory.create(new Locale("pt", "BR"));
		   Analyzer cogroo = factory.createPipe();
		   
		   essayList.get(0).extractFeatures(cogroo);
		   
		   /*tools t = new tools();
	       
		   List<String> s = t.word2syllables("uma");
		   System.out.println(s.size());
		   for (String i: s){
		      System.out.println(i);
		   }*/

		   //System.out.println("teste");
		   // System.out.println(t.stressed_syll("bueiro"));
	   }
}
