package grammarpt;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.cogroo.analyzer.Analyzer;
import org.cogroo.checker.CheckDocument;
import org.cogroo.checker.GrammarChecker;
import org.cogroo.entities.Mistake;
import org.cogroo.text.Sentence;
import org.cogroo.text.Token;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.giullianomorroni.jcorretor.CorretorOrtografico;
import com.giullianomorroni.jcorretor.SugestaoOrtografia;

//import com.memetix.mst.language.Language;
//import com.memetix.mst.translate.Translate;
import grammarpt.tools;

public class Essay {
	
	private String bodyText;
	private float finalGrade;
	private String generalComment;
	private String specificComments;
	
	private tools toolObj = new tools();
	
	private HashMap skills = new HashMap();
	
	public enum SkillTypes{
	     UNDERSTANDING, SELECTINGINFO, SHOWKNOW, SOLUTION, FORMAL 	
	}
	
	//private Analyzer cogroo;
	//ComponentFactory factory;
	
	Essay(File f) throws SAXException, IOException{
		DocumentBuilderFactory docFac = DocumentBuilderFactory.newInstance();
		//this.factory = ComponentFactory.create(new Locale("pt", "BR"));
		//this.cogroo = factory.createPipe();
		 
	       
		try {
			
			DocumentBuilder docBuilder = docFac.newDocumentBuilder();
			Document document = docBuilder.parse(f);
		
			NodeList body = document.getElementsByTagName("body").item(0).getChildNodes();
			
			String bodyText = "";
			for(int k=0;k<body.getLength();k++){
				 String tagName = body.item(k).getNodeName();
				 String textContent = body.item(k).getTextContent().trim();
				 if (tagName == "wrong" || tagName == "#text"){
					 bodyText = bodyText.concat(" ");
					 bodyText = bodyText.concat(textContent);
				 }
	        }
			
			this.bodyText = bodyText;
			System.out.println("-->" +  f.getAbsolutePath());
			String finalgrade = document.getElementsByTagName("finalgrade").item(0).getTextContent();
			this.finalGrade = Float.parseFloat(finalgrade.replace(',', '.'));
			
			NodeList skillList = document.getElementsByTagName("skills").item(0).getChildNodes();
			for(int k=0;k<skillList.getLength();k++){
				 //String tagName = skillList.item(k).getNodeName();
				
				Node nameFirst = skillList.item(k).getFirstChild();
				Node nameLast = skillList.item(k).getLastChild();
				float gradeSkill = 0;
				if (nameLast != null ){
					String gradeSkillStr = nameLast.getTextContent().trim();
			
					if (!gradeSkillStr.isEmpty()){
				        gradeSkill = Float.parseFloat(nameLast.getTextContent().replace(',', '.'));
					}else gradeSkill = 0;
				}
				if (nameFirst != null){
				    String desc = nameFirst.getTextContent();
				    if (desc.contains("Selecionar")){
				    	skills.put(SkillTypes.SELECTINGINFO, new Double(gradeSkill));
				    }else if (desc.contains("Demonstrar c")) {
				    	skills.put(SkillTypes.SHOWKNOW, new Double(gradeSkill));
					}else if (desc.contains("Compreender")){
						skills.put(SkillTypes.UNDERSTANDING, new Double(gradeSkill));
					}else if (desc.contains("Elaborar")){
						skills.put(SkillTypes.SOLUTION, new Double(gradeSkill));
					}else if (desc.contains("Demonstrar d")){
						skills.put(SkillTypes.FORMAL, new Double(gradeSkill));
					}
				}
				
	        }
			
			
			this.generalComment = document.getElementsByTagName("generalcomment").item(0).getTextContent();
			this.specificComments = document.getElementsByTagName("specificaspects").item(0).getTextContent();
			
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public int numSyllable(List<Token> lstTokens){
		int nSyllable = 0;
		
		for(Token t: lstTokens){
			String [] lsttokens = t.getLexeme().split("_");
			for (String w: lsttokens){
				
			    ArrayList<String> s = toolObj.word2syllables(w);
				nSyllable = nSyllable + s.size();
			}

		}
		return nSyllable;
	}
	
	public int fleschScore(float syllPerWord, float wordsPerSent){
        int fs = 0;
        double score = 206.835 - (84.6 * syllPerWord) - (1.015 * wordsPerSent) + 42;  
        
        return fs;
	}
	
	public void extractFeatures(Analyzer c) throws IllegalArgumentException, IOException{
		
		GrammarChecker gc = new GrammarChecker(c);
		
		CheckDocument doc = new CheckDocument(this.bodyText);
		
		gc.analyze(doc);
		
		
		
		// as features of mistakes (remove space errors): number of grammar mistakes, 
		// number of mistakes per number of words
		// number of syllabes per word
		// number of words per sentence
		
		List<Mistake> mlist = doc.getMistakes();
		
		int ntokens = 0;
		int wordlenghtSum = 0;
		float avgsyll = 0;
		List<Sentence> lstSentence = doc.getSentences(); 
		for (Sentence s : lstSentence){
			List<Token> lstTokens = s.getTokens();
			avgsyll = avgsyll + numSyllable(lstTokens);
			ntokens = ntokens + lstTokens.size();
			for (Token t : lstTokens){
			    wordlenghtSum = wordlenghtSum + t.toString().length();
			}
		}
		int nErrors = mlist.size();
		float nErrorsTokens =  nErrors / ntokens;
		avgsyll = avgsyll / ntokens;
		float tokensperSentence = lstSentence.size() / ntokens ;
		
		double fs = fleschScore(avgsyll, tokensperSentence);
		
		// average word length
		int avgWordLength = wordlenghtSum / ntokens;
		
		// number of spelling mistakes
		// number of spelling mistakes per number of words
		CorretorOrtografico corretor = new CorretorOrtografico();
        SugestaoOrtografia correcao = corretor.corrigir(this.bodyText);
		List<String> lstCorrecao = correcao.getTextoSugerido();
		
		int nSpelling = lstCorrecao.size();
		int nSpellingTokens = nSpelling / ntokens;
		
		// Flesh score
		
		
	}
	
	
}
