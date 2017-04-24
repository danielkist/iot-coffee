
package me.dkist.iot.web.batch;

import java.io.IOException;
import java.time.LocalDateTime;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import me.dkist.iot.web.batch.Batch.BatchStatus;
import me.dkist.iot.web.person.Person;
import me.dkist.iot.web.person.PersonRepository;
import me.dkist.iot.web.slack.SlackService;

@RestController
@RequestMapping("/api/v1/batch")
public class BatchController {
	
	// @Autowired MongoTemplate mongoTemplate;
	@Autowired BatchRepository batchRepository;

	@Autowired PersonRepository personRepository;
	
	@Autowired SlackService slackService;

	@RequestMapping(value = "/{status}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public Document coffeeMachineStatus(@PathVariable BatchStatus status) throws IOException {
		if (status.equals(BatchStatus.IN_PRODUCTION)) {
			Batch b = batchRepository.findOpenBatch();
			if (b == null) {
				b = new Batch();
				b.setInsertTime(LocalDateTime.now());
			}
			b.setInitTime(LocalDateTime.now());
			b.setStatus(status);
			batchRepository.save(b);
		} else if (status.equals(BatchStatus.COMPLETED)) {
			Batch b = batchRepository.findOpenBatch();
			if (b != null) {
				b.setStatus(status);
				b.setEndTime(LocalDateTime.now());
				b.setNumberOfCups(38); // TODO Implement calc for cups/time
				batchRepository.save(b);
				slackService.notifyPersons(b.getNotify());
				
				String mkr = "@johndoe";
				if(b.getMaker() != null) {
					Person maker = personRepository.findById(b.getMaker());
					mkr = maker.getSlackUser();
				}
				slackService.notifyMaker(mkr);
			}
		}
		return new Document("status", "ok");
	}

}