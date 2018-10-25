package com.tcs.nltk.coreference_resolution;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;

public class App {

	class TaggedToken {
		String tag;
		String token;

		public TaggedToken(String tag, String token) {
			super();
			this.tag = tag;
			this.token = token;
		}

		@Override
		public String toString() {
			return "[" + tag + "=" + token + "]";
		}

	}

	public void process() throws IOException {
		ClassLoader classLoader = getClass().getClassLoader();
		SentenceModel sent_model = new SentenceModel(
				new FileInputStream(classLoader.getResource("en-sent.bin").getFile()));
		SentenceDetectorME sentenceDetector = new SentenceDetectorME(sent_model);
		TokenizerModel token_model = new TokenizerModel(
				new FileInputStream(classLoader.getResource("en-token.bin").getFile()));
		Tokenizer tokenizer = new TokenizerME(token_model);
		POSModel pos_model = new POSModel(new FileInputStream(classLoader.getResource("en-pos-maxent.bin").getFile()));
		POSTaggerME tagger = new POSTaggerME(pos_model);

		String input = new String(
				Files.readAllBytes(Paths.get(classLoader.getResource("input2").getFile().substring(1))));

		input = input.replaceAll("Crestor 10 mg", "CRESTOR");
		input = input.replaceAll("Crestor", "CRESTOR");
		input = input.replaceAll("brand Crestor", "CRESTOR");
		input = input.replaceAll("generic Crestor", "CRESTOR");
		input = input.replaceAll("Toprol XL 50 mg", "TOPROL");
		input = input.replaceAll("Nexium Rx capsule", "NEXIUM");
		input = input.replaceAll("Nexium Rx", "NEXIUM");

		input = input.replaceAll("\\\\n", " ").replaceAll("\\\\'s", "");
		String sentences[] = sentenceDetector.sentDetect(input);

		for (String sentence : Arrays.asList(sentences)) {
			List<TaggedToken> taggedTokens = new ArrayList<>();
			System.out.println(sentence);
			String tokens[] = tokenizer.tokenize(sentence);
			String tags[] = tagger.tag(tokens);
			for (int i = 0; i < tokens.length; i++) {
				if (isDrug(tokens[i]))
					tags[i] = "DRUG";
				if (isDate(tokens[i]))
					tags[i] = "DATE";
				taggedTokens.add(new TaggedToken(tags[i], tokens[i]));
			}
			System.out.println(taggedTokens.toString());
			taggedTokens = generateRelation(taggedTokens);

			System.out.println(taggedTokens.toString());
			System.out.println();
		}
	}

	List<String> chunkRegex;

	{
		chunkRegex = new ArrayList<>();
		chunkRegex.add("OBJECT:<IN>(<JJ>)*<NN>|(<DRUG>)");
		chunkRegex.add("ACTION_DATE:(<IN>)*<DATE>");
		chunkRegex.add("ACTION:(<VB[A-Z]*>)+");
		chunkRegex.add("SUBJECT:(<TO>)*(<DT>)*(<JJ[A-Z]*)*(<NN>|<NNP>|<NNS>|<NNPS>)+");
	}

	private List<TaggedToken> generateRelation(List<TaggedToken> taggedTokens) {
		StringBuffer tagChain = getTagChain(taggedTokens);
		for (String pattern : chunkRegex) {
			String tag = pattern.split(":")[0];
			String regex = pattern.split(":")[1];
			Pattern p = Pattern.compile(regex);
			Matcher m = p.matcher(tagChain);
			while (m.find()) {
				int count = (tagChain.substring(m.start(), m.end()).split("<").length - 1);
				int start = (tagChain.substring(0, m.start()).split("<").length - 1);
				String new_token = "";
				for (int idx = 0; idx < count; idx++) {
					new_token += taggedTokens.get(start).token + " ";
					taggedTokens.remove(start);
				}
				taggedTokens.add(start, new TaggedToken(tag, new_token.trim()));
				tagChain = getTagChain(taggedTokens);
				m = p.matcher(tagChain);
			}
		}
		return taggedTokens;
	}

	private StringBuffer getTagChain(List<TaggedToken> taggedTokens) {
		String tagChain = "";
		for (TaggedToken entry : taggedTokens) {
			tagChain += "<" + entry.tag + ">";
		}
		return new StringBuffer(tagChain);
	}

	private boolean isDrug(String string) {
		if (string.equals("CRESTOR") || string.equals("TOPROL") || string.equals("NEXIUM"))
			return true;
		return false;
	}

	private boolean isDate(String string) {
		string = string.replaceAll("\\)", "").replaceAll("\\(", "");
		if (" Jan2008 Dec2017 Nov2000 2003 03-Jan-2018 03Jan2018 2003 ".replaceAll(string, "").contains("  "))
			return true;
		return false;
	}

	public static void main(String[] args) throws IOException {
		new App().process();
	}

}
