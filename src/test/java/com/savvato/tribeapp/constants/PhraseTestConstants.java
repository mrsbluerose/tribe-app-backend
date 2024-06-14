package com.savvato.tribeapp.constants;

import com.savvato.tribeapp.entities.*;

public interface PhraseTestConstants {

    long ADVERB1_ID = 100L;
    long ADVERB2_ID = 200L;
    long ADVERB3_ID = 300L;

    long VERB1_ID = 100L;
    long VERB2_ID = 200L;
    long VERB3_ID = 300L;

    long PREPOSITION1_ID = 100L;
    long PREPOSITION2_ID = 200L;
    long PREPOSITION3_ID = 300L;

    long NOUN1_ID = 100L;
    long NOUN2_ID = 200L;
    long NOUN3_ID = 300L;

    String ADVERB1_WORD = "testAdverb1";
    String ADVERB2_WORD = "testAdverb2";
    String ADVERB3_WORD = "testAdverb3";

    String VERB1_WORD = "testVerb1";
    String VERB2_WORD = "testVerb2";
    String VERB3_WORD = "testVerb3";

    String PREPOSITION1_WORD = "testPreposition1";
    String PREPOSITION2_WORD = "testPreposition2";
    String PREPOSITION3_WORD = "testPreposition3";

    String NOUN1_WORD = "testNoun1";
    String NOUN2_WORD = "testNoun2";
    String NOUN3_WORD = "testNoun3";

    long PHRASE1_ID = 1L;
    long PHRASE2_ID = 2L;
    long PHRASE3_ID = 3L;

    static Adverb getTestAdverb1() {
        Adverb rtn = new Adverb();
        rtn.setId(ADVERB1_ID);
        rtn.setWord(ADVERB1_WORD);
        return rtn;
    }

    static Adverb getTestAdverb2() {
        Adverb rtn = new Adverb();
        rtn.setId(ADVERB2_ID);
        rtn.setWord(ADVERB2_WORD);
        return rtn;
    }

    static Adverb getTestAdverb3() {
        Adverb rtn = new Adverb();
        rtn.setId(ADVERB3_ID);
        rtn.setWord(ADVERB3_WORD);
        return rtn;
    }

    static Verb getTestVerb1() {
        Verb rtn = new Verb();
        rtn.setId(VERB1_ID);
        rtn.setWord(VERB1_WORD);
        return rtn;
    }

    static Verb getTestVerb2() {
        Verb rtn = new Verb();
        rtn.setId(VERB2_ID);
        rtn.setWord(VERB2_WORD);
        return rtn;
    }

    static Verb getTestVerb3() {
        Verb rtn = new Verb();
        rtn.setId(VERB3_ID);
        rtn.setWord(VERB3_WORD);
        return rtn;
    }

    static Preposition getTestPreposition1() {
        Preposition rtn = new Preposition();
        rtn.setId(PREPOSITION1_ID);
        rtn.setWord(PREPOSITION1_WORD);
        return rtn;
    }

    static Preposition getTestPreposition2() {
        Preposition rtn = new Preposition();
        rtn.setId(PREPOSITION2_ID);
        rtn.setWord(PREPOSITION2_WORD);
        return rtn;
    }

    static Preposition getTestPreposition3() {
        Preposition rtn = new Preposition();
        rtn.setId(PREPOSITION3_ID);
        rtn.setWord(PREPOSITION3_WORD);
        return rtn;
    }

    static Noun getTestNoun1() {
        Noun rtn = new Noun();
        rtn.setId(NOUN1_ID);
        rtn.setWord(NOUN1_WORD);
        return rtn;
    }

    static Noun getTestNoun2() {
        Noun rtn = new Noun();
        rtn.setId(NOUN2_ID);
        rtn.setWord(NOUN2_WORD);
        return rtn;
    }

    static Noun getTestNoun3() {
        Noun rtn = new Noun();
        rtn.setId(NOUN3_ID);
        rtn.setWord(NOUN3_WORD);
        return rtn;
    }

    static Phrase getTestPhrase1() {
        Phrase testPhrase = new Phrase();
        testPhrase.setId(PHRASE1_ID);
        testPhrase.setAdverbId(ADVERB1_ID);
        testPhrase.setVerbId(VERB1_ID);
        testPhrase.setPrepositionId(PREPOSITION1_ID);
        testPhrase.setNounId(NOUN1_ID);
        return testPhrase;
    }

    static Phrase getTestPhrase2() {
        Phrase testPhrase = new Phrase();
        testPhrase.setId(PHRASE2_ID);
        testPhrase.setAdverbId(ADVERB2_ID);
        testPhrase.setVerbId(VERB2_ID);
        testPhrase.setPrepositionId(PREPOSITION2_ID);
        testPhrase.setNounId(NOUN2_ID);
        return testPhrase;
    }

    static Phrase getTestPhrase3() {
        Phrase testPhrase = new Phrase();
        testPhrase.setId(PHRASE3_ID);
        testPhrase.setAdverbId(ADVERB3_ID);
        testPhrase.setVerbId(VERB3_ID);
        testPhrase.setPrepositionId(PREPOSITION3_ID);
        testPhrase.setNounId(NOUN3_ID);
        return testPhrase;
    }

}
