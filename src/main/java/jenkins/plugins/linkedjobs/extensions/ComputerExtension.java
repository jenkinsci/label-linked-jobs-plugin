package jenkins.plugins.linkedjobs.extensions;

import java.util.Collection;
import java.util.Collections;

import jenkins.plugins.linkedjobs.actions.ComputerLinkedJobsAction;
import hudson.Extension;
import hudson.model.Action;
import hudson.model.Computer;
import hudson.model.TransientComputerActionFactory;

/**
 * The only role of this extension/factory is to instantiate a {@link ComputerLinkedJobsAction}
 * for each computer/node of this Jenkins instance.
 * @author domi
 *
 */

@Extension
public class ComputerExtension extends TransientComputerActionFactory {
    
    @Override
    public Collection<? extends Action> createFor(Computer target) {
        return Collections.singletonList(new ComputerLinkedJobsAction(target));
    }
}
