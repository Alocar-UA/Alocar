package pt.ua.tqs.alocar.acceptance;

import static io.cucumber.junit.platform.engine.Constants.GLUE_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.PLUGIN_PROPERTY_NAME;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

/**
 * Runs the Gherkin scenarios in src/test/resources/features (acceptance tests of the user stories).
 * Scenarios are tagged with the story (e.g. @US03) and the business rules they check (e.g. @BR6).
 * Reports go to target/cucumber-reports (the JSON can be imported into Xray as Cucumber results).
 */
@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "pt.ua.tqs.alocar.acceptance")
@ConfigurationParameter(
        key = PLUGIN_PROPERTY_NAME,
        value = "pretty, json:target/cucumber-reports/cucumber.json, junit:target/cucumber-reports/cucumber.xml")
class CucumberIT {}
