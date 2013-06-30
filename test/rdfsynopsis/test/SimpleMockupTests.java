package rdfsynopsis.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ ClassHierarchyTest.class, OntologyRatioTest.class,
		SimpleTest.class,
		TypedSubjectRatioTest.class, BlankNodesTest.class,
		PropertyUsagePerSubjectClassTest.class, PredicatVocabulariesTest.class })
public class SimpleMockupTests {

}
