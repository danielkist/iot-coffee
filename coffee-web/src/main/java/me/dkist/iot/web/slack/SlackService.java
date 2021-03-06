package me.dkist.iot.web.slack;

import java.io.IOException;
import java.util.List;

import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import in.ashwanthkumar.slack.webhook.Slack;
import in.ashwanthkumar.slack.webhook.SlackMessage;
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
		persons.forEach(personId -> {
			Person p = personRepository.findById(personId);
			if(p != null) {
				sendSlackNotification(p.getSlackUser(), "Coffee is ready!");
			}
		});
	}
	
	@Async
	public void notifyMaker(String slackUser) {
		sleep(30000);
		sendSlackNotification("#coffee", "Coffee is ready! Thanks to " + slackUser);
	}

	private void sendSlackNotification(String channel, String message) {
		logger.info("Sending to {} message: {}", channel, message);
		if(channel.equals("@johndoe")) return;
		
		String url = slackWebhookUrl;
		if(url == null) {
			logger.error("Slack Webhook not found");
			return;
		}
		logger.info("Webhook: {}", url);
		Slack slack = new Slack(url);
		try {
			if(channel.startsWith("#")) {
				slack.sendToChannel(channel).push(new SlackMessage(message));
			} else if(channel.startsWith("@")) {
				slack.sendToUser(channel).push(new SlackMessage(message));
			}
		} catch (IOException e) { 
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
