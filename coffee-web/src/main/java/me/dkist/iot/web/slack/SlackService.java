package me.dkist.iot.web.slack;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import me.dkist.iot.web.person.Person;
import me.dkist.iot.web.person.PersonRepository;

@Component
public class SlackService {
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Value("${slack.webhook.url}")
	private String slackWebhookUrl;
	
	@Autowired PersonRepository personRepository;
	
	@Async
	public void notifyPersons(List<ObjectId> persons) {
		if (persons == null || persons.size() == 0) return;
		logger.info("Yay, let's tell people that we have coffee!");
		Person maker = personRepository.findById(persons.remove(0));
		sendSlackNotification(maker.getSlackUser(), "Coffee is ready!");
		if(persons.size() > 0) sleep(30000);
		persons.forEach(personId -> {
			Person p = personRepository.findById(personId);
			if(p != null) {
				sendSlackNotification(p.getSlackUser(), "Coffee is ready!");
			}
		});
		sleep(60000);
		sendSlackNotification("#coffee", "Coffee is ready! Thanks to " + maker.getSlackUser());
		
	}

	private void sendSlackNotification(String channel, String message) {
		logger.info("Sending to {} message: {}", channel, message);
		if(channel.equals("@johndoe")) return;
		
		String url = slackWebhookUrl;
		if(url == null || url.length() == 0) url = System.getenv("SLACK_WEBHOOK_URL");
		if(url == null) {
			logger.error("Slack Webhook not found");
			return;
		}
        slackWebhookRequest(url, channel, message);
	}
	
	private void slackWebhookRequest(String webhook, String channel, String message) {
		String payload = "{\"channel\": \"%s\", \"text\": \"%s\" }";
		HttpClient httpclient = HttpClientBuilder.create().build();
        HttpPost request = new HttpPost(webhook);
        try {
        	 List<NameValuePair> formparams = new ArrayList<NameValuePair>();
        	 formparams.add(new BasicNameValuePair("payload", String.format(payload, channel, message)));
        	 UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams);
             request.setEntity(entity);
             HttpResponse response = httpclient.execute(request);
             HttpEntity returnEntity = response.getEntity();
             String responseString = EntityUtils.toString(returnEntity);
             System.out.println(responseString);
        } catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void sleep(Integer delay) {
		try {
			Thread.sleep(delay);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
