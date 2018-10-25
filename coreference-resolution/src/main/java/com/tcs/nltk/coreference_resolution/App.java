package com.tcs.nltk.coreference_resolution;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;

import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;

/**
 * Hello world!
 *
 */
public class App {

	public void process() throws IOException {
		ClassLoader classLoader = getClass().getClassLoader();
		SentenceModel sent_model = new SentenceModel(new FileInputStream(classLoader.getResource("en-sent.bin").getFile()));
		SentenceDetectorME sentenceDetector = new SentenceDetectorME(sent_model);
		TokenizerModel token_model = new TokenizerModel(new FileInputStream(classLoader.getResource("en-token.bin").getFile()));
		Tokenizer tokenizer = new TokenizerME(token_model);
		POSModel pos_model = new POSModel(new FileInputStream(classLoader.getResource("en-pos-maxent.bin").getFile()));
		POSTaggerME tagger = new POSTaggerME(pos_model);

		String input = "Call came directly to the IC for medical inquiry and adverse event noted for Crestor. Patient started Crestor 10 mg daily by mouth in Jan2008 for high cholesterol. Patient also taking Toprol XL 50 mg daily by mouth started in Nov2000 for high blood pressure. Patient reported\nthe following that she has skipped doses of Crestor over the years since she started taking it and the last time was in Dec2017. She would just take her usual dose the next day and not try to make up the missed dose. Her HCP is aware, no treatment offered. Had high\ncholesterol since about 2003 and currently taking brand Crestor. She is nervous now about taking new medications, unknown start date and if her HCP is aware. Patient to start generic Crestor when finish her brand Crestor. No further information was provided.\nFollow up received on 03-Jan-2018.\nUpdated on 03Jan2018. The following should have been included in the above narrative. Patient stated that she is fine with the brand Crestor, she no problems with it. This information was received in the original phone call. Same sender table.";
		input = input.replaceAll("\\n", " ");
		String sentences[] = sentenceDetector.sentDetect(input);
		for (String sentence : Arrays.asList(sentences)) {
			System.out.println(sentence);
			String tokens[] = tokenizer.tokenize(sentence);
			String tags[] = tagger.tag(tokens);
			for (int i = 0; i < tokens.length; i++) {
				System.out.print("[" + tokens[i] + " : " + tags[i] + "],");
			}
			System.out.println();
			System.out.println();
		}
	}

	public static void main(String[] args) throws IOException {
		new App().process();
	}
}
