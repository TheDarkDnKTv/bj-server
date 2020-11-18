package thedarkdnktv.openbjs.api.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.Node;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginConfiguration;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.config.plugins.validation.constraints.Required;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.apache.logging.log4j.core.layout.PatternMatch;
import org.apache.logging.log4j.core.layout.PatternSelector;
import org.apache.logging.log4j.core.pattern.PatternFormatter;
import org.apache.logging.log4j.core.pattern.PatternParser;
import org.apache.logging.log4j.util.PerformanceSensitive;

@Plugin(name= "LoggerNameSelector", category = Node.CATEGORY, elementType = PatternSelector.ELEMENT_TYPE)
@PerformanceSensitive("allocation")
public class LoggerNameSelector implements PatternSelector {
	
	private class LogSelector {
        private final String name;
        private final boolean isPackage;
        private final PatternFormatter[] formatters;

        LogSelector(String name, PatternFormatter[] formatters) {
            this.name = name;
            this.isPackage = name.endsWith(".");
            this.formatters = formatters;
        }

        PatternFormatter[] get() {
            return this.formatters;
        }

        boolean test(String s) {
            return this.isPackage ? s.startsWith(this.name) : s.equals(this.name);
        }
	}
	
	private final PatternFormatter[] defaultFormatters;
	private final List<LogSelector> formatters = new ArrayList<>();
	
	public LoggerNameSelector(String defaultPattern, PatternMatch[] properties, Configuration config) {
		PatternParser parser = PatternLayout.createPatternParser(config);
        this.defaultFormatters = toArray(parser.parse(defaultPattern));
        for (PatternMatch property : properties)
        {
            PatternFormatter[] formatters = toArray(parser.parse(property.getPattern()));
            for (String name : property.getKey().split(","))
            {
                this.formatters.add(new LogSelector(name, formatters));
            }
        }
	}
	
	private PatternFormatter[] toArray(List<PatternFormatter> list) {
		return list.toArray(new PatternFormatter[list.size()]);
	}
	
	@Override
	public PatternFormatter[] getFormatters(LogEvent event) {
		final String loggerName = event.getLoggerName();
		
        if (loggerName != null) {
            for (int i = 0; i < this.formatters.size(); i++) {
            	LogSelector selector = this.formatters.get(i);
                if (selector.test(loggerName))
                {
                    return selector.get();
                }
            }
        }
		
		return this.defaultFormatters;
	}
	
	@PluginFactory
	public static LoggerNameSelector createSelector(
			@Required @PluginAttribute(value = "defaultPattern") String defaultPattern,
			@PluginElement("PatternMatch") PatternMatch[] properties,
			@PluginConfiguration Configuration config) {
		
		return new LoggerNameSelector(defaultPattern, properties, config);
	}
}
