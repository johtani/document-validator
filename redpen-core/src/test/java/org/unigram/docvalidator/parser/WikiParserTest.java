/**
 * redpen: a text inspection tool
 * Copyright (C) 2014 Recruit Technologies Co., Ltd. and contributors
 * (see CONTRIBUTORS.md)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.unigram.docvalidator.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.unigram.docvalidator.config.Configuration;
import org.unigram.docvalidator.model.Document;
import org.unigram.docvalidator.config.ValidationConfigurationLoader;
import org.unigram.docvalidator.model.ListBlock;
import org.unigram.docvalidator.model.Paragraph;
import org.unigram.docvalidator.model.Section;
import org.unigram.docvalidator.config.CharacterTable;
import org.unigram.docvalidator.config.CharacterTableLoader;
import org.unigram.docvalidator.config.ValidatorConfiguration;
import org.unigram.docvalidator.DocumentValidatorException;

import static org.unigram.docvalidator.parser.Parser.Type.WIKI;

public class WikiParserTest {

  @Before
  public void setup() {
  }

  @Test
  public void testBasicDocument() throws UnsupportedEncodingException {
    String sampleText = "";
    sampleText += "h1. About Gekioko.\n";
    sampleText += "Gekioko pun pun maru means very very angry.\n";
    sampleText += "\n";
    sampleText += "The word also have posive meaning.\n";
    sampleText += "h2. About Gunma.\n";
    sampleText += "\n";
    sampleText += "Gunma is located at west of Saitama.\n";
    sampleText += "- Features\n";
    sampleText += "-- Main City: Gumma City\n";
    sampleText += "-- Capical: 200 Millon\n";
    sampleText += "- Location\n";
    sampleText += "-- Japan\n";
    sampleText += "\n";
    sampleText += "The word also have posive meaning. Hower it is a bit wired.";

    Document doc = createFileContent(sampleText);
    assertEquals(3, doc.getNumberOfSections());
    // first section
    final Section firstSection = doc.getSection(0);
    assertEquals(1, firstSection.getHeaderContentsListSize());
    assertEquals("", firstSection.getHeaderContent(0).content);
    assertEquals(0, firstSection.getNumberOfLists());
    assertEquals(0, firstSection.getNumberOfParagraphs());
    assertEquals(1, firstSection.getNumberOfSubsections());

    // 2nd section
    final Section secondSection = doc.getSection(1);
    assertEquals(1, secondSection.getHeaderContentsListSize());
    assertEquals("About Gekioko.", secondSection.getHeaderContent(0).content);
    assertEquals(0, secondSection.getNumberOfLists());
    assertEquals(2, secondSection.getNumberOfParagraphs());
    assertEquals(1, secondSection.getNumberOfSubsections());
    assertEquals(firstSection, secondSection.getParentSection());
    // validate paragraph in 2nd section
    assertEquals(1, secondSection.getParagraph(0).getNumberOfSentences());
    assertEquals(true, secondSection.getParagraph(0).getSentence(0).isFirstSentence);
    assertEquals(1, secondSection.getParagraph(1).getNumberOfSentences());
    assertEquals(true, secondSection.getParagraph(1).getSentence(0).isFirstSentence);

    // last section
    Section lastSection = doc.getSection(doc.getNumberOfSections()-1);
    assertEquals(1, lastSection.getNumberOfLists());
    assertEquals(5, lastSection.getListBlock(0).getNumberOfListElements());
    assertEquals(2,lastSection.getNumberOfParagraphs());
    assertEquals(1, lastSection.getHeaderContentsListSize());
    assertEquals(0, lastSection.getNumberOfSubsections());
    assertEquals("About Gunma.", lastSection.getHeaderContent(0).content);
    assertEquals(secondSection, lastSection.getParentSection());

    // validate paragraph in last section
    assertEquals(1, lastSection.getParagraph(0).getNumberOfSentences());
    assertEquals(true, lastSection.getParagraph(0).getSentence(0).isFirstSentence);
    assertEquals(2, lastSection.getParagraph(1).getNumberOfSentences());
    assertEquals(true, lastSection.getParagraph(1).getSentence(0).isFirstSentence);
    assertEquals(false, lastSection.getParagraph(1).getSentence(1).isFirstSentence);

  }

  @Test
  public void testGenerateDocumentWithList() {
    String sampleText =
        "Threre are several railway companies in Japan as follows.\n";
    sampleText += "- Tokyu\n";
    sampleText += "-- Toyoko Line\n";
    sampleText += "-- Denentoshi Line\n";
    sampleText += "- Keio\n";
    sampleText += "- Odakyu\n";
    Document doc = createFileContent(sampleText);
    assertEquals(5, doc.getSection(0).getListBlock(0).getNumberOfListElements());
    assertEquals("Tokyu", doc.getSection(0).getListBlock(0).getListElement(0).getSentence(0).content);
    assertEquals(1, doc.getSection(0).getListBlock(0).getListElement(0).getLevel());
    assertEquals("Toyoko Line", doc.getSection(0).getListBlock(0).getListElement(1).getSentence(0).content);
    assertEquals(2, doc.getSection(0).getListBlock(0).getListElement(1).getLevel());
    assertEquals("Denentoshi Line", doc.getSection(0).getListBlock(0).getListElement(2).getSentence(0).content);
    assertEquals(2, doc.getSection(0).getListBlock(0).getListElement(2).getLevel());
    assertEquals("Keio", doc.getSection(0).getListBlock(0).getListElement(3).getSentence(0).content);
    assertEquals(1, doc.getSection(0).getListBlock(0).getListElement(3).getLevel());
    assertEquals("Odakyu", doc.getSection(0).getListBlock(0).getListElement(4).getSentence(0).content);
    assertEquals(1, doc.getSection(0).getListBlock(0).getListElement(4).getLevel());
  }

  @Test
  public void testGenerateDocumentWithNumberedList() {
    String sampleText =
        "Threre are several railway companies in Japan as follows.\n";
    sampleText += "# Tokyu\n";
    sampleText += "## Toyoko Line\n";
    sampleText += "## Denentoshi Line\n";
    sampleText += "# Keio\n";
    sampleText += "# Odakyu\n";
    Document doc = createFileContent(sampleText);
    assertEquals(5, doc.getSection(0).getListBlock(0).getNumberOfListElements());
    assertEquals("Tokyu", doc.getSection(0).getListBlock(0).getListElement(0).getSentence(0).content);
    assertEquals(1, doc.getSection(0).getListBlock(0).getListElement(0).getLevel());
    assertEquals("Toyoko Line", doc.getSection(0).getListBlock(0).getListElement(1).getSentence(0).content);
    assertEquals(2, doc.getSection(0).getListBlock(0).getListElement(1).getLevel());
    assertEquals("Denentoshi Line", doc.getSection(0).getListBlock(0).getListElement(2).getSentence(0).content);
    assertEquals(2, doc.getSection(0).getListBlock(0).getListElement(2).getLevel());
    assertEquals("Keio", doc.getSection(0).getListBlock(0).getListElement(3).getSentence(0).content);
    assertEquals(1, doc.getSection(0).getListBlock(0).getListElement(3).getLevel());
    assertEquals("Odakyu", doc.getSection(0).getListBlock(0).getListElement(4).getSentence(0).content);
    assertEquals(1, doc.getSection(0).getListBlock(0).getListElement(4).getLevel());
  }

  @Test
  public void testGenerateDocumentWithOneLineComment() {
    String sampleText =
        "There are various tests.\n";
    sampleText += "[!-- The following should be exmples --]\n";
    sampleText += "Most common one is unit test.\n";
    sampleText += "Integration test is also common.\n";
    Document doc = createFileContent(sampleText);
    Section firstSections = doc.getSection(0);
    Paragraph firstParagraph = firstSections.getParagraph(0);
    assertEquals(3, firstParagraph.getNumberOfSentences());
  }

  @Test
  public void testGenerateDocumentWithMultiLinesComment() {
    String sampleText =
        "There are various tests.\n";
    sampleText += "[!-- \n";
    sampleText += "The following should be exmples\n";
    sampleText += "--]\n";
    sampleText += "Most common one is unit test.\n";
    sampleText += "Integration test is also common.\n";
    Document doc = createFileContent(sampleText);
    Section firstSections = doc.getSection(0);
    Paragraph firstParagraph = firstSections.getParagraph(0);
    assertEquals(3, firstParagraph.getNumberOfSentences());
  }

  @Test
  public void testGenerateDocumentWithMultiLinesComment2() {
    String sampleText =
        "There are various tests.\n";
    sampleText += "[!-- \n";
    sampleText += "The following should be exmples\n";
    sampleText += "In addition the histories should be described\n";
    sampleText += "--]\n";
    sampleText += "Most common one is unit test.\n";
    sampleText += "Integration test is also common.\n";
    Document doc = createFileContent(sampleText);
    Section firstSections = doc.getSection(0);
    Paragraph firstParagraph = firstSections.getParagraph(0);
    assertEquals(3, firstParagraph.getNumberOfSentences());
  }

  @Test
  public void testGenerateDocumentWithVoidComment() {
    String sampleText =
        "There are various tests.\n";
    sampleText += "[!----]\n";
    sampleText += "Most common one is unit test.\n";
    sampleText += "Integration test is also common.\n";
    Document doc = createFileContent(sampleText);
    Section firstSections = doc.getSection(0);
    Paragraph firstParagraph = firstSections.getParagraph(0);
    assertEquals(3, firstParagraph.getNumberOfSentences());
  }

  @Test
  public void testGenerateDocumentWithOnlySpaceComment() {
    String sampleText =
        "There are various tests.\n";
    sampleText += "[!-- --]\n";
    sampleText += "Most common one is unit test.\n";
    sampleText += "Integration test is also common.\n";
    Document doc = createFileContent(sampleText);
    Section firstSections = doc.getSection(0);
    Paragraph firstParagraph = firstSections.getParagraph(0);
    assertEquals(3, firstParagraph.getNumberOfSentences());
  }

  @Test
  public void testGenerateDocumentWithCommentHavingHeadSpace() {
    String sampleText =
        "There are various tests.\n";
    sampleText += " [!-- BLAH BLAH --]\n";
    sampleText += "Most common one is unit test.\n";
    sampleText += "Integration test is also common.\n";
    Document doc = createFileContent(sampleText);
    Section firstSections = doc.getSection(0);
    Paragraph firstParagraph = firstSections.getParagraph(0);
    assertEquals(3, firstParagraph.getNumberOfSentences());
  }

  @Test
  public void testGenerateDocumentWithCommentHavingTailingSpace() {
    String sampleText =
        "There are various tests.\n";
    sampleText += "[!-- BLAH BLAH --] \n";
    sampleText += "Most common one is unit test.\n";
    sampleText += "Integration test is also common.\n";
    Document doc = createFileContent(sampleText);
    Section firstSections = doc.getSection(0);
    Paragraph firstParagraph = firstSections.getParagraph(0);
    assertEquals(3, firstParagraph.getNumberOfSentences());
  }

  @Test
  public void testGenerateDocumentWithMultiLinesCommentHavingSpaces() {
    String sampleText =
        "There are various tests.\n";
    sampleText += " [!-- \n";
    sampleText += "The following should be exmples\n";
    sampleText += "In addition the histories should be described\n";
    sampleText += "--] \n";
    sampleText += "Most common one is unit test.\n";
    sampleText += "Integration test is also common.\n";
    Document doc = createFileContent(sampleText);
    Section firstSections = doc.getSection(0);
    Paragraph firstParagraph = firstSections.getParagraph(0);
    assertEquals(3, firstParagraph.getNumberOfSentences());
  }

  @Test
  public void testGenerateDocumentWithMultipleSentenceInOneSentence() {
    String sampleText =
        "Tokyu is a good railway company. The company is reliable. In addition it is rich.";
    String[] expectedResult = {"Tokyu is a good railway company.",
        " The company is reliable.", " In addition it is rich."};
    Document doc = createFileContent(sampleText);
    Section firstSections = doc.getSection(0);
    Paragraph firstParagraph = firstSections.getParagraph(0);
    assertEquals(3, firstParagraph.getNumberOfSentences());
    for (int i=0; i<expectedResult.length; i++) {
      assertEquals(expectedResult[i], firstParagraph.getSentence(i).content);
    }
  }

  @Test
  public void testGenerateDocumentWithMultipleSentences() {
    String sampleText = "Tokyu is a good railway company. The company is reliable. In addition it is rich.\n";
    sampleText += "I like the company. Howerver someone does not like it.";
    Document doc = createFileContent(sampleText);
    Section firstSections = doc.getSection(0);
    Paragraph firstParagraph = firstSections.getParagraph(0);
    assertEquals(5, firstParagraph.getNumberOfSentences());
  }

  @Test
  public void testGenerateDocumentWithMultipleSentencesWithVaraiousStopCharacters() {
    String sampleText = "Is Tokyu a good railway company? The company is reliable. In addition it is rich!\n";
    Document doc = createFileContent(sampleText);
    Section firstSections = doc.getSection(0);
    Paragraph firstParagraph = firstSections.getParagraph(0);
    assertEquals(3, firstParagraph.getNumberOfSentences());
    assertEquals("Is Tokyu a good railway company?", doc.getSection(0).getParagraph(0).getSentence(0).content);
    assertEquals(" The company is reliable.", doc.getSection(0).getParagraph(0).getSentence(1).content);
    assertEquals(" In addition it is rich!", doc.getSection(0).getParagraph(0).getSentence(2).content);
  }

  @Test
  public void testGenerateDocumentWitVoidContent() {
    String sampleText = "";
    Document doc = createFileContent(sampleText);
    Section firstSections = doc.getSection(0);
    assertEquals(0, firstSections.getParagraphs().size());
//    assertEquals(false, firstSections.getParagraphs().hasNext());
  }

  @Test
  public void testGenerateDocumentWithPeriodInSuccession() {
    String sampleText = "...";
    Document doc = createFileContent(sampleText);
    Section firstSections = doc.getSection(0);
    Paragraph firstParagraph = firstSections.getParagraph(0);
    assertEquals(1, firstParagraph.getNumberOfSentences());
  }

  @Test
  public void testGenerateDocumentWitoutPeriodInLastSentence() {
    String sampleText = "Hongo is located at the west of Tokyo. Saitama is located at the north";
    Document doc = createFileContent(sampleText);
    Section firstSections = doc.getSection(0);
    Paragraph firstParagraph = firstSections.getParagraph(0);
    assertEquals(2, firstParagraph.getNumberOfSentences());
  }

  @Test
  public void testGenerateDocumentWithSentenceLongerThanOneLine() {
    String sampleText = "This is a good day.\n";
    sampleText += "Hongo is located at the west of Tokyo ";
    sampleText += "which is the capital of Japan ";
    sampleText += "which is not located in the south of the earth.";
    Document doc = createFileContent(sampleText);
    Section firstSections = doc.getSection(0);
    Paragraph firstParagraph = firstSections.getParagraph(0);
    assertEquals(2, firstParagraph.getNumberOfSentences());
  }

  @Test
  public void testPlainLink() {
    String sampleText = "this is not a [[pen]], but also this is not [[Google|http://google.com]] either.";
    Document doc = createFileContent(sampleText);
    Section firstSections = doc.getSection(0);
    Paragraph firstParagraph = firstSections.getParagraph(0);
    assertEquals(1, firstParagraph.getNumberOfSentences());
    assertEquals(2, firstParagraph.getSentence(0).links.size());
    assertEquals("pen", firstParagraph.getSentence(0).links.get(0));
    assertEquals("http://google.com", firstParagraph.getSentence(0).links.get(1));
    assertEquals("this is not a pen, but also this is not Google either.",
        firstParagraph.getSentence(0).content);
  }

  @Test
  public void testPlainLinkWithSpaces() {
    String sampleText = "the url is not [[Google | http://google.com ]].";
    Document doc = createFileContent(sampleText);
    Section firstSections = doc.getSection(0);
    Paragraph firstParagraph = firstSections.getParagraph(0);
    assertEquals(1, firstParagraph.getNumberOfSentences());
    assertEquals(1, firstParagraph.getSentence(0).links.size());
    assertEquals("http://google.com", firstParagraph.getSentence(0).links.get(0));
    assertEquals("the url is not Google.",
        firstParagraph.getSentence(0).content);
  }

  @Test
  public void testLinkWithoutTag() {
    String sampleText = "url of google is [[http://google.com]].";
    Document doc = createFileContent(sampleText);
    Section firstSections = doc.getSection(0);
    Paragraph firstParagraph = firstSections.getParagraph(0);
    assertEquals(1, firstParagraph.getNumberOfSentences());
    assertEquals(1, firstParagraph.getSentence(0).links.size());
    assertEquals("http://google.com", firstParagraph.getSentence(0).links.get(0));
    assertEquals("url of google is http://google.com.",
        firstParagraph.getSentence(0).content);
  }

  @Test
  public void testIncompleteLink() {
    String sampleText = "url of google is [[http://google.com.";
    Document doc = createFileContent(sampleText);
    Section firstSections = doc.getSection(0);
    Paragraph firstParagraph = firstSections.getParagraph(0);
    assertEquals(1, firstParagraph.getNumberOfSentences());
    assertEquals(0, firstParagraph.getSentence(0).links.size());
    assertEquals("url of google is [[http://google.com.",
        firstParagraph.getSentence(0).content);
  }

  @Test
  public void testPlainLinkWithThreeBlock() {
    String sampleText = "this is not a pen, but also this is not [[Google|http://google.com|dummy]] either.";
    Document doc = createFileContent(sampleText);
    Section firstSections = doc.getSection(0);
    Paragraph firstParagraph = firstSections.getParagraph(0);
    assertEquals(1, firstParagraph.getNumberOfSentences());
    assertEquals(1, firstParagraph.getSentence(0).links.size());
    assertEquals("http://google.com", firstParagraph.getSentence(0).links.get(0));
    assertEquals("this is not a pen, but also this is not Google either.",
        firstParagraph.getSentence(0).content);
  }

  @Test
  public void testVacantListBlock() {
    String sampleText = "this is not a pen, but also this is not [[]] Google either.";
    Document doc = createFileContent(sampleText);
    Section firstSections = doc.getSection(0);
    Paragraph firstParagraph = firstSections.getParagraph(0);
    assertEquals(1, firstParagraph.getNumberOfSentences());
    assertEquals(1, firstParagraph.getSentence(0).links.size());
    assertEquals("", firstParagraph.getSentence(0).links.get(0));
    assertEquals("this is not a pen, but also this is not  Google either.",
        firstParagraph.getSentence(0).content);
  }

  @Test
  public void testDocumentWithItalicWord() {
    String sampleText = "This is a //good// day.\n";
    Document doc = createFileContent(sampleText);
    Section firstSections = doc.getSection(0);
    Paragraph firstParagraph = firstSections.getParagraph(0);
    assertEquals("This is a good day.", firstParagraph.getSentence(0).content);
  }

  @Test
  public void testDocumentWithMultipleItalicWords() {
    String sampleText = "//This// is a //good// day.\n";
    Document doc = createFileContent(sampleText);
    Section firstSections = doc.getSection(0);
    Paragraph firstParagraph = firstSections.getParagraph(0);
    assertEquals("This is a good day.", firstParagraph.getSentence(0).content);
  }

  @Test
  public void testDocumentWithMultipleNearItalicWords() {
    String sampleText = "This is //a// //good// day.\n";
    Document doc = createFileContent(sampleText);
    Section firstSections = doc.getSection(0);
    Paragraph firstParagraph = firstSections.getParagraph(0);
    assertEquals("This is a good day.", firstParagraph.getSentence(0).content);
  }

  @Test
  public void testDocumentWithItalicExpression() {
    String sampleText = "This is //a good// day.\n";
    Document doc = createFileContent(sampleText);
    Section firstSections = doc.getSection(0);
    Paragraph firstParagraph = firstSections.getParagraph(0);
    assertEquals("This is a good day.", firstParagraph.getSentence(0).content);
  }

  @Test
  public void testDocumentWithHeaderCotainingMultipleSentences()
      throws UnsupportedEncodingException {
    String sampleText = "";
    sampleText += "h1. About Gunma. About Saitama.\n";
    sampleText += "Gunma is located at west of Saitama.\n";
    sampleText += "The word also have posive meaning. Hower it is a bit wired.";

    Document doc = createFileContent(sampleText);
    Section lastSection = doc.getSection(doc.getNumberOfSections()-1);
    assertEquals(2, lastSection.getHeaderContentsListSize());
    assertEquals("About Gunma.", lastSection.getHeaderContent(0).content);
    assertEquals(" About Saitama.", lastSection.getHeaderContent(1).content);
  }

  @Test
  public void testDocumentWithHeaderWitoutPeriod()
      throws UnsupportedEncodingException {
    String sampleText = "";
    sampleText += "h1. About Gunma\n";
    sampleText += "Gunma is located at west of Saitama.\n";
    sampleText += "The word also have posive meaning. Hower it is a bit wired.";

    Document doc = createFileContent(sampleText);
    Section lastSection = doc.getSection(doc.getNumberOfSections()-1);
    assertEquals(1, lastSection.getHeaderContentsListSize());
    assertEquals("About Gunma", lastSection.getHeaderContent(0).content);
  }

  @Test
  public void testDocumentWithList()
      throws UnsupportedEncodingException {
    String sampleText = "";
    sampleText += "h1. About Gunma. About Saitama.\n";
    sampleText += "- Gunma is located at west of Saitama.\n";
    sampleText += "- The word also have posive meaning. Hower it is a bit wired.";

    Document doc = createFileContent(sampleText);
    Section lastSection = doc.getSection(doc.getNumberOfSections()-1);
    ListBlock listBlock = lastSection.getListBlock(0);
    assertEquals(2, listBlock.getNumberOfListElements());
    assertEquals(1, listBlock.getListElement(0).getNumberOfSentences());
    assertEquals("Gunma is located at west of Saitama.",
        listBlock.getListElement(0).getSentence(0).content);
    assertEquals("The word also have posive meaning.",
        listBlock.getListElement(1).getSentence(0).content);
    assertEquals(" Hower it is a bit wired.",
        listBlock.getListElement(1).getSentence(1).content);
  }

  @Test
  public void testDocumentWithListWithoutPeriod()
      throws UnsupportedEncodingException {
    String sampleText = "";
    sampleText += "h1. About Gunma. About Saitama.\n";
    sampleText += "- Gunma is located at west of Saitama\n";

    Document doc = createFileContent(sampleText);
    Section lastSection = doc.getSection(doc.getNumberOfSections()-1);
    ListBlock listBlock = lastSection.getListBlock(0);
    assertEquals(1, listBlock.getNumberOfListElements());
    assertEquals(1, listBlock.getListElement(0).getNumberOfSentences());
    assertEquals("Gunma is located at west of Saitama",
        listBlock.getListElement(0).getSentence(0).content);
  }

  @Test
  public void testDocumentWithMultipleSections()
      throws UnsupportedEncodingException {
    String sampleText = "h1. Prefectures in Japan.\n";
    sampleText += "There are 47 prefectures in Japan.\n";
    sampleText += "\n";
    sampleText += "Each prefectures has its features.\n";
    sampleText += "h2. Gunma \n";
    sampleText += "Gumma is very beautiful";

    Document doc = createFileContent(sampleText);
    assertEquals(3, doc.getNumberOfSections());
    Section rootSection = doc.getSection(0);
    Section h1Section = doc.getSection(1);
    Section h2Section = doc.getSection(2);

    assertEquals(0, rootSection.getLevel());
    assertEquals(1, h1Section.getLevel());
    assertEquals(2, h2Section.getLevel());

    assertEquals(rootSection.getSubSection(0), h1Section);
    assertEquals(h1Section.getParentSection(), rootSection);
    assertEquals(h2Section.getParentSection(), h1Section);
    assertEquals(rootSection.getParentSection(), null);

    assertEquals(0, rootSection.getHeaderContent(0).position);
    assertEquals(0, h1Section.getHeaderContent(0).position);
    assertEquals(4, h2Section.getHeaderContent(0).position);
  }

  @Test
  public void testGenerateJapaneseDocument() {
    String japaneseConfiguraitonStr = "" +
      "<?xml version=\"1.0\"?>" +
      "<component name=\"Validator\">" +
      "</component>";

    String japaneseCharTableStr = "" +
      "<?xml version=\"1.0\"?>" +
      "<character-table>" +
      "<character name=\"FULL_STOP\" value=\"。\" />" +
      "</character-table>";

    String sampleText = "埼玉は東京の北に存在する。";
    sampleText += "大きなベッドタウンであり、多くの人が住んでいる。";
    Document doc = null;

    try {
      doc = createFileContent(sampleText, japaneseConfiguraitonStr,
          japaneseCharTableStr);
    } catch (DocumentValidatorException e1) {
      e1.printStackTrace();
      fail();
    }

    Section firstSections = doc.getSection(0);
    Paragraph firstParagraph = firstSections.getParagraph(0);
    assertEquals(2, firstParagraph.getNumberOfSentences());
  }

  private Parser loadParser(Configuration configuration) {
    Parser parser = null;
    try {
      parser = DocumentParserFactory.generate(WIKI, configuration);
    } catch (DocumentValidatorException e1) {
      fail();
      e1.printStackTrace();
    }
    return parser;
  }

  private Document createFileContent(String inputDocumentString,
      String configurationString,
      String characterTableString) throws DocumentValidatorException {
    InputStream configStream = IOUtils.toInputStream(configurationString);
    ValidatorConfiguration conf =
        ValidationConfigurationLoader.loadConfiguration(configStream);

    CharacterTable characterTable = null;
    if (characterTableString.length() > 0) {
      InputStream characterTableStream =
          IOUtils.toInputStream(characterTableString);
      characterTable = CharacterTableLoader.load(characterTableStream);
    }
    return createFileContent(inputDocumentString, conf, characterTable);
  }

  private Document createFileContent(String inputDocumentString,
      ValidatorConfiguration conf,
      CharacterTable characterTable) {
    InputStream inputDocumentStream = null;
    try {
      inputDocumentStream =
          new ByteArrayInputStream(inputDocumentString.getBytes("utf-8"));
    } catch (UnsupportedEncodingException e1) {
      fail();
    }

    Parser parser = null;
    if (characterTable != null) {
      parser = loadParser(new Configuration(conf, characterTable));
    } else {
      parser = loadParser(new Configuration(conf));
    }

    try {
      return parser.generateDocument(inputDocumentStream);
    } catch (DocumentValidatorException e) {
      e.printStackTrace();
      return null;
    }
  }

  private Document createFileContent(
      String inputDocumentString) {
    ValidatorConfiguration conf = new ValidatorConfiguration("dummy");
    Parser parser = loadParser(new Configuration(conf));
    InputStream is;
    try {
      is = new ByteArrayInputStream(inputDocumentString.getBytes("utf-8"));
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
      return null;
    }
    Document doc = null;
    try {
      doc = parser.generateDocument(is);
    } catch (DocumentValidatorException e) {
      e.printStackTrace();
    }
    return doc;
  }

}
