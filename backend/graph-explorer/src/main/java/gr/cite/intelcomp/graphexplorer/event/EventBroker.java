package gr.cite.intelcomp.graphexplorer.event;

import gr.cite.commons.web.oidc.apikey.events.ApiKeyStaleEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
public class EventBroker {
	@Autowired
	private ApplicationEventPublisher applicationEventPublisher;

	public void emitApiKeyStale(String apiKey) {
		this.applicationEventPublisher.publishEvent(new ApiKeyStaleEvent(apiKey));
	}

	public void emit(ApiKeyStaleEvent event) {
		this.applicationEventPublisher.publishEvent(event);
	}

	public void emit(UserTouchedEvent event) {
		this.applicationEventPublisher.publishEvent(event);
	}

	public void emit(NodeTouchedEvent event) {
		this.applicationEventPublisher.publishEvent(event);
	}
	public void emit(EdgeTouchedEvent event) {
		this.applicationEventPublisher.publishEvent(event);
	}
}
