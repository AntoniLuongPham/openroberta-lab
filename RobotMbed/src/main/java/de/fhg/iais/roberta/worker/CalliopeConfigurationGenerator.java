package de.fhg.iais.roberta.worker;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import de.fhg.iais.roberta.bean.NewUsedHardwareBean;
import de.fhg.iais.roberta.components.ConfigurationAst;
import de.fhg.iais.roberta.components.ConfigurationComponent;
import de.fhg.iais.roberta.components.Project;
import de.fhg.iais.roberta.syntax.Phrase;
import de.fhg.iais.roberta.visitor.collect.MbedReplaceVisitor;

public class CalliopeConfigurationGenerator implements IWorker {

    @Override
    public void execute(Project project) {
        if ( project.isDefaultConfiguration() ) {
            NewUsedHardwareBean.Builder usedHardwareBeanBuilder = new NewUsedHardwareBean.Builder();

            List<List<Phrase<Void>>> lists = recreateAst(project.getProgramAst().getTree(), project.getConfigurationAst().getConfigurationComponentsValues(), usedHardwareBeanBuilder);

            // TODO this replaces the old phrases with the new ones -> create new programAST or replace or smth?
            project.getProgramAst().getTree().clear();
            project.getProgramAst().getTree().addAll(lists);

            ConfigurationAst.Builder builder = new ConfigurationAst.Builder();
            builder.addComponents(usedHardwareBeanBuilder.build().getUsedConfigurationComponents());
            builder.setXmlVersion(project.getConfigurationAst().getXmlVersion());
            builder.setTags(project.getConfigurationAst().getTags());
            builder.setRobotType(project.getConfigurationAst().getRobotType());
            builder.setDescription(project.getConfigurationAst().getDescription());
            project.addConfigurationAst(builder.build());
        }
    }

    private static List<List<Phrase<Void>>> recreateAst(
        List<List<Phrase<Void>>> tree, Collection<ConfigurationComponent> defaultComponents, NewUsedHardwareBean.Builder builder) {

        MbedReplaceVisitor mbedReplaceVisitor = new MbedReplaceVisitor(builder, defaultComponents);

        // TODO expressions!! -> all blocks need a Builder/modify method

        List<List<Phrase<Void>>> newTree = new ArrayList<>(tree.size());
        for ( List<Phrase<Void>> phrases : tree ) {
            List<Phrase<Void>> newPhrases = new ArrayList<>(phrases.size());
            for ( Phrase<Void> phrase : phrases ) {
                phrase.accept(mbedReplaceVisitor);
                if ( mbedReplaceVisitor.wasPhraseReplaced()) {
                    newPhrases.add(mbedReplaceVisitor.popLastReplacedPhrase());
                } else {
                    newPhrases.add(phrase);
                }
            }
            newTree.add(newPhrases);
        }
        return newTree;
    }
}
