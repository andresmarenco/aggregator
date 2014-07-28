package aggregator.sampler;

import java.text.MessageFormat;
import java.util.List;

import org.joda.time.LocalDateTime;

import aggregator.beans.QueryResult;
import aggregator.beans.Vertical;
import aggregator.sampler.indexer.AbstractSamplerIndexer;
import aggregator.util.FileWriterHelper;
import aggregator.util.delay.NullDelay;
import aggregator.verticalwrapper.AbstractVerticalWrapper;
import aggregator.verticalwrapper.VerticalWrapperController;

public class QBS extends AbstractSampler {

	/**
	 * Default Constructor
	 */
	public QBS() {
	}
	
	@Override
	public void execute(Vertical vertical) {
		String timestampPattern = "yyyy/MM/dd HH:ss.SSS";
		try
		{
			try(FileWriterHelper executionLog = this.createExecutionLog(vertical)) {
				executionLog.open(false);
				executionLog.writeLine(MessageFormat.format("[{0}] Starting sampling with {1} algorithm...", LocalDateTime.now().toString(timestampPattern), this.getSamplerName()));
				
				AbstractVerticalWrapper verticalWrapper = VerticalWrapperController.getInstance().createVerticalWrapper(vertical);
				AbstractSamplerIndexer samplerIndexer = AbstractSamplerIndexer.newInstance(vertical, this);
				
				
				List<QueryResult> result = verticalWrapper.executeQuery(this.currentSeed);
				
				
				if(!(delayHandler instanceof NullDelay)) {
					executionLog.writeLine(MessageFormat.format("[{0}] Starting timeout...", LocalDateTime.now().toString(timestampPattern)));
					this.delayHandler.executeDelay();
					executionLog.writeLine(MessageFormat.format("[{0}] Timeout finished", LocalDateTime.now().toString(timestampPattern)));
				}
				
			}
		}
		catch(Exception ex) {
			log.error(ex.getMessage(), ex);
		}
	}

	@Override
	public String getSamplerCodeName() {
		return "QBS";
	}

	@Override
	public String getSamplerName() {
		return "Query-Based Sampler";
	}

}
