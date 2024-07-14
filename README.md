# GPT-4o APP for Android

This sample demonstrates how to make speech on GPT-4o with Java using the Speech SDK for Android.

## Need replace the parameters("***") in java file. eg: subscribe key etc.. 

* MainActivity.java
  
<code>private static final String SpeechSubscriptionKey = "xxx";
 pprivate static final String SpeechRegion = "xxx";</code>


* ChatAPI.java
  
<code>URL url = new URL("https://xxx.openai.azure.com/openai/deployments/gpt-4o/chat/completions?api-version=2024-02-15-preview"); // Replace with actual API endpoint
    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
    connection.setRequestMethod("POST");
    connection.setRequestProperty("Content-Type", "application/json");
    connection.setRequestProperty("api-key", "xxx"); 
</code>    
## References

* [Speech SDK API reference for Java](https://aka.ms/csspeech/javaref)
