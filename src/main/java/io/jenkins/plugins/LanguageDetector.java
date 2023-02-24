package io.jenkins.plugins;
import hudson.model.AbstractBuild;
import org.kohsuke.github.*;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.tasks.Builder;
import hudson.tasks.BuildStepDescriptor;
import hudson.Extension;
import hudson.DescriptorExtensionList;
import hudson.model.Descriptor;
import java.util.Map;
import jenkins.model.Jenkins;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import hudson.util.FormValidation;
import org.kohsuke.stapler.QueryParameter;
import hudson.Launcher;
import net.sf.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import org.kohsuke.stapler.StaplerRequest;
import java.net.URL;
import java.net.MalformedURLException;
import java.io.IOException;
import hudson.model.Node;
import hudson.model.Computer;
import hudson.slaves.DumbSlave;
import hudson.slaves.JNLPLauncher;
import hudson.slaves.RetentionStrategy;
import java.util.Collections;
import java.util.concurrent.TimeUnit;


@Symbol("languageDetector")
public class LanguageDetector extends Builder {

    private static final String DEFAULT_LANGUAGE = "unknown";
    private static final int DEFAULT_TERMINATION_TIME = 60;

    private String apiEndpoint;
    private String personalToken;
    private String repoURL;

    @DataBoundConstructor
    public LanguageDetector(String apiEndpoint, String personalToken, String repoURL) {
        this.apiEndpoint = apiEndpoint;
        this.personalToken = personalToken;
        this.repoURL = repoURL;
    }

    @DataBoundSetter
    public void setApiEndpoint(String apiEndpoint) {
        this.apiEndpoint = apiEndpoint;
    }

    @DataBoundSetter
    public void setPersonalToken(String personalToken) {
        this.personalToken = personalToken;
    }

    @DataBoundSetter
    public void setrepoURL(String repoURL) {
        this.repoURL = repoURL;
    }

    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
        try {
            // Create a Jenkins instance to interact with Jenkins API
            Jenkins jenkins = Jenkins.get();

            // Check if the agent node is available and has been running for less than an hour
            boolean isNodeAvailable = false;
            listener.getLogger().println("Checking if agent node is available and has been running for less than an hour ");
            for (Node node : jenkins.getNodes()) {
                if (node instanceof DumbSlave && node.toComputer().isOnline() && node.toComputer().getChannel() != null) {
                    if (node.toComputer().isOnline()) {
                        Computer computer = node.toComputer();
                        long uptime = System.currentTimeMillis() - computer.getConnectTime();
                        if (uptime < TimeUnit.HOURS.toMillis(1)) {
                            isNodeAvailable = true;
                            break;
                        }
                    }
                }
            }

            if (!isNodeAvailable) {
                // Create a new Jenkins slave node
                listener.getLogger().println("Slave Node is not available");
                DumbSlave newSlave = new DumbSlave("New Slave", "New Slave created by LanguageDetector plugin",
                        "/home/jenkins", "1", Node.Mode.NORMAL, "", new JNLPLauncher(), RetentionStrategy.INSTANCE, Collections.emptyList());
                jenkins.addNode(newSlave);
                listener.getLogger().println("A new Jenkins slave node has been created.");
            }

            try {
                String language = detectLanguage(this.repoURL);
                listener.getLogger().println("Detected language: " + language);
                listener.getLogger().println("Started to Trigger API");

                if (StringUtils.isEmpty(this.apiEndpoint)) {
                    this.apiEndpoint = "http://gogole.com";
                }

                String apiURL = this.apiEndpoint + "?language=" + language;
                listener.getLogger().println("Triggering API: " + apiURL);
                triggerApi(apiURL);
                System.out.println("Success");
                listener.getLogger().println("Successful - Triggering API: " + apiURL);

            } catch (Exception e) {
                System.out.println("An error occurred while making API request: " + e.getMessage());
                listener.getLogger().println("Failed - Triggering API: ");
            }

        } catch (Exception e) {
            listener.getLogger().println(ExceptionUtils.getStackTrace(e));
            return false;
        }
        return true;
    }
    
    private String detectLanguage(String repoUrl) throws Exception {
        // Parse the GitHub repository URL
        try {
            String[] urlParts = repoUrl.split("/");
            String owner = urlParts[urlParts.length - 2];
            String repoName = urlParts[urlParts.length - 1];

            // Authenticate with the GitHub API using a personal access token
            GitHub github = new GitHubBuilder().withOAuthToken(personalToken).build();

            // Retrieve the repository's language statistics
            GHRepository repo = github.getRepository(owner + "/" + repoName);
            Map<String, Long> languages = repo.listLanguages();

            // Find the language with the most bytes
            String mostUsedLanguage = "";
            long mostBytes = 0;
            for (String language : languages.keySet()) {
                Long bytes = languages.get(language);
                if (bytes > mostBytes) {
                    mostUsedLanguage = language;
                    mostBytes = bytes;
                }
            }

            // Return the most used language
            return mostUsedLanguage;
        } catch (IOException e) {
           
        }
        return null;
     }
 

        /**
     * This method sends a POST request to the specified API endpoint and prints the response.
     *
     * @param apiEndpoint The URL of the API endpoint to trigger.
     * @throws IOException If there is an error making the API request.
     */
    private void triggerApi(String apiEndpoint) throws IOException {
        // Create a URL object from the API endpoint string.
        URL url = new URL(apiEndpoint);

        // Open a connection to the API endpoint URL.
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        // Set the HTTP request method to POST.
        connection.setRequestMethod("POST");

        // Set the connection timeout to 5 seconds.
        connection.setConnectTimeout(5000);

        // Set the read timeout to 5 seconds.
        connection.setReadTimeout(5000);

        // Connect to the API endpoint.
        connection.connect();

        // Get the HTTP response code from the API endpoint.
        int responseCode = connection.getResponseCode();

        // If the response code is 200 OK, read and print the response from the API.
        if (responseCode == HttpURLConnection.HTTP_OK) {
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            System.out.println(response.toString());
        } else {
            // If the response code is not 200 OK, print an error message.
            System.out.println("API request failed with response code: " + responseCode);
        }
    }


    
    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {
        private String apiEndpoint;
        private String personalToken;
        private String repoURL;

        public DescriptorImpl() {
            load();
        }

        // Getter and setter methods for apiEndpoint
        public String getApiEndpoint() {
            return apiEndpoint;
        }
        
        public void setApiEndpoint(String apiEndpoint) {
            this.apiEndpoint = apiEndpoint;
            save();
        }

        // Getter and setter methods for personalToken
        public String getPersonalToken() {
            return personalToken;
        }

        public void setPersonalToken(String personalToken) {
            this.personalToken = personalToken;
            save();
        }
         // Getter and setter methods for personalToken
        public String getrepoURL() {
            return repoURL;
        }

        public void setrepoURL(String repoURL) {
            this.repoURL = repoURL;
            save();
        }
        
                

        
        @Override
        public LanguageDetector newInstance(StaplerRequest req, JSONObject formData) throws FormException {
            LanguageDetector languageDetector = (LanguageDetector) super.newInstance(req, formData);

            // Get values from form data
            String apiEndpoint = formData.getString("apiEndpoint");
            String personalToken = formData.getString("personalToken");
            String repoURL = formData.getString("repoURL");
            

            // Set values in LanguageDetector object
            languageDetector.setApiEndpoint(apiEndpoint);
            languageDetector.setPersonalToken(personalToken);
            languageDetector.setrepoURL(repoURL);
            

            return languageDetector;
        }


        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            // indicates that this builder can be used with all kinds of project types
            return true;
        }

        public String getDisplayName() {
            return "Detect Language";
        }

        // Config form fields
        public DescriptorExtensionList<Builder, Descriptor<Builder>> getBuilderDescriptors() {
            return Jenkins.get().getDescriptorList(Builder.class);
        }

        public FormValidation doTestApiEndpoint(@QueryParameter String apiEndpoint) {
            if (StringUtils.isEmpty(apiEndpoint)) {
                return FormValidation.error("API endpoint must not be empty");
            }
            try {
                URL url = new URL(apiEndpoint);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);
                connection.connect();

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    return FormValidation.ok("API endpoint is valid");
                } else {
                    return FormValidation.error("API call failed with error code: " + responseCode);
                }
            } catch (MalformedURLException e) {
                return FormValidation.error("Invalid URL");
            } catch (IOException e) {
                return FormValidation.error("Error connecting to API: " + ExceptionUtils.getStackTrace(e));
            }
        }

      

        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
            setApiEndpoint(formData.getString("apiEndpoint"));
            setPersonalToken(formData.getString("personalToken"));
            setrepoURL(formData.getString("repoURL"));
            save();
            return super.configure(req, formData);
        }

       
    }

 
 }
       
