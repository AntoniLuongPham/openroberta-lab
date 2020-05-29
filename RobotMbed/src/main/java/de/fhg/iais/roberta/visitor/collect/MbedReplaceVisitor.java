package de.fhg.iais.roberta.visitor.collect;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import de.fhg.iais.roberta.bean.NewUsedHardwareBean;
import de.fhg.iais.roberta.components.ConfigurationComponent;
import de.fhg.iais.roberta.syntax.Phrase;
import de.fhg.iais.roberta.syntax.action.Action;
import de.fhg.iais.roberta.syntax.action.display.ShowTextAction;
import de.fhg.iais.roberta.syntax.action.mbed.DisplayTextAction;
import de.fhg.iais.roberta.syntax.lang.expr.ActionExpr;
import de.fhg.iais.roberta.syntax.lang.expr.Binary;
import de.fhg.iais.roberta.syntax.lang.expr.Expr;
import de.fhg.iais.roberta.syntax.lang.expr.FunctionExpr;
import de.fhg.iais.roberta.syntax.lang.expr.MathConst;
import de.fhg.iais.roberta.syntax.lang.expr.NumConst;
import de.fhg.iais.roberta.syntax.lang.expr.SensorExpr;
import de.fhg.iais.roberta.syntax.lang.functions.Function;
import de.fhg.iais.roberta.syntax.lang.functions.MathRandomIntFunct;
import de.fhg.iais.roberta.syntax.sensor.Sensor;
import de.fhg.iais.roberta.syntax.sensor.SensorMetaDataBean;
import de.fhg.iais.roberta.syntax.sensor.generic.UltrasonicSensor;
import de.fhg.iais.roberta.util.dbc.Assert;
import de.fhg.iais.roberta.worker.DefaultConfigurationHelper;

// TODO base on ISensorVisitor<Void>, IAllActorsVisitor<Void>, ILanguageVisitor<Void>
public class MbedReplaceVisitor implements IMbedCollectorVisitor {

    protected final NewUsedHardwareBean.Builder builder;

    private final Collection<ConfigurationComponent> defaultComponents;

    private Phrase<Void> lastReplacedPhrase = null;

    public MbedReplaceVisitor(NewUsedHardwareBean.Builder builder, Collection<ConfigurationComponent> defaultComponents) {
        this.builder = builder;
        this.defaultComponents = defaultComponents;
    }

    public boolean wasPhraseReplaced() {
        return this.lastReplacedPhrase != null;
    }

    public Phrase<Void> popLastReplacedPhrase() {
        Assert.notNull(this.lastReplacedPhrase, "No phrase was replaced beforehand!");
        Phrase<Void> phrase = this.lastReplacedPhrase;
        this.lastReplacedPhrase = null;
        return phrase;
    }

    public Expr<Void> getLastReplacedAsExpr() {
        if ( this.lastReplacedPhrase instanceof Sensor ) {
            return SensorExpr.make((Sensor<Void>) popLastReplacedPhrase());
        } else if ( this.lastReplacedPhrase instanceof Action ) {
            return ActionExpr.make((Action<Void>) popLastReplacedPhrase());
        } else if ( this.lastReplacedPhrase instanceof Function ){
            return FunctionExpr.make((Function<Void>) popLastReplacedPhrase());
        } else { // TODO all expression types
            return (Expr<Void>) popLastReplacedPhrase();
        }
    }

//    @Override
//    public Void visitPinWriteValueAction(PinWriteValueAction<Void> pinWriteValueAction) {
//        ConfigurationComponent defaultComponent =
//            DefaultConfigurationHelper
//                .getDefaultComponent(
//                    this.defaultComponents,
//                    pinWriteValueAction.getKind().getName(),
//                    pinWriteValueAction.getMode(),
//                    pinWriteValueAction.getPort());
//        this.builder.addUsedConfigurationComponent(defaultComponent);
//        return IMbedCollectorVisitor.super.visitPinWriteValueAction(pinWriteValueAction);
//    }
//
//    @Override
//    public Void visitPinSetPullAction(PinSetPullAction<Void> pinSetPullAction) {
//        ConfigurationComponent defaultComponent =
//            DefaultConfigurationHelper
//                .getDefaultComponent(this.defaultComponents, pinSetPullAction.getKind().getName(), pinSetPullAction.getMode(), pinSetPullAction.getPort());
//        this.builder.addUsedConfigurationComponent(defaultComponent);
//        return IMbedCollectorVisitor.super.visitPinSetPullAction(pinSetPullAction);
//    }
//
//    @Override
//    public Void visitLedOnAction(LedOnAction<Void> ledOnAction) {
//        ConfigurationComponent defaultComponent =
//            DefaultConfigurationHelper.getDefaultComponent(this.defaultComponents, ledOnAction.getKind().getName(), "", ledOnAction.getPort());
//        this.builder.addUsedConfigurationComponent(defaultComponent);
//        return IMbedCollectorVisitor.super.visitLedOnAction(ledOnAction);
//    }
//
//    @Override
//    public Void visitLightAction(LightAction<Void> lightAction) {
//        ConfigurationComponent defaultComponent =
//            DefaultConfigurationHelper
//                .getDefaultComponent(this.defaultComponents, lightAction.getKind().getName(), lightAction.getMode().toString(), lightAction.getPort());
//        this.builder.addUsedConfigurationComponent(defaultComponent);
//        LightAction<Void> phrase =
//            LightAction
//                .make(
//                    defaultComponent.getUserDefinedPortName(),
//                    lightAction.getColor(),
//                    lightAction.getMode(),
//                    lightAction.getRgbLedColor(),
//                    lightAction.getProperty(),
//                    lightAction.getComment());
//
//        this.lastReplacedPhrase = phrase;
//        return IMbedCollectorVisitor.super.visitLightAction(phrase);
//    }
//
//    @Override
//    public Void visitLightStatusAction(LightStatusAction<Void> lightStatusAction) {
//        ConfigurationComponent defaultComponent =
//            DefaultConfigurationHelper
//                .getDefaultComponent(
//                    this.defaultComponents,
//                    lightStatusAction.getKind().getName(),
//                    lightStatusAction.getStatus().toString(),
//                    lightStatusAction.getPort());
//        this.builder.addUsedConfigurationComponent(defaultComponent);
//        return IMbedCollectorVisitor.super.visitLightStatusAction(lightStatusAction);
//    }

    @Override
    public Void visitUltrasonicSensor(UltrasonicSensor<Void> ultrasonicSensor) {
        // TODO find a different way to declare this
        // as only port names of sensors matching specific criteria should be added it has to happen here or in the generalized "replacer/modifier"
        ConfigurationComponent defaultComponent =
            DefaultConfigurationHelper
                .getDefaultComponent(this.defaultComponents, ultrasonicSensor.getKind().getName(), ultrasonicSensor.getMode(), ultrasonicSensor.getPort());

        // needed to decide which configuration components the program actually uses, to only show those in the configuration
        this.builder.addUsedConfigurationComponent(defaultComponent);

        // TODO find a way to replace this nicely with modify or builder
        this.lastReplacedPhrase =
            UltrasonicSensor
                .make(
                    new SensorMetaDataBean(defaultComponent.getUserDefinedPortName(), ultrasonicSensor.getMode(), ultrasonicSensor.getSlot(), false),
                    ultrasonicSensor.getProperty(),
                    ultrasonicSensor.getComment());
        return null;
    }

    @Override
    public Void visitDisplayTextAction(DisplayTextAction<Void> displayTextAction) {
        displayTextAction.getMsg().accept(this);
        Expr<Void> msg = getLastReplacedAsExpr();

        // TODO create copies of all attributes? should be handled in builder/modify if necessary
        this.lastReplacedPhrase = DisplayTextAction.make(displayTextAction.getMode(), msg, displayTextAction.getProperty(), displayTextAction.getComment());
        return null;
    }

    @Override
    public Void visitShowTextAction(ShowTextAction<Void> showTextAction) {
        // this should only be needed for the robots that have an "external" display accessed through a port
        ConfigurationComponent defaultComponent =
            DefaultConfigurationHelper.getDefaultComponent(this.defaultComponents, showTextAction.getKind().getName(), "", showTextAction.getPort());
        this.builder.addUsedConfigurationComponent(defaultComponent);

        showTextAction.getMsg().accept(this);
        Expr<Void> msg = getLastReplacedAsExpr();
        showTextAction.getX().accept(this);
        Expr<Void> x = getLastReplacedAsExpr();
        showTextAction.getY().accept(this);
        Expr<Void> y = getLastReplacedAsExpr();

        this.lastReplacedPhrase = ShowTextAction.make(msg, x, y, showTextAction.getPort(), showTextAction.getProperty(), showTextAction.getComment());
        return null;
    }

    @Override
    public Void visitBinary(Binary<Void> binary) {
        binary.getRight().accept(this);
        Expr<Void> right = getLastReplacedAsExpr();
        binary.getLeft().accept(this);
        Expr<Void> left = getLastReplacedAsExpr();

        this.lastReplacedPhrase = Binary.make(binary.getOp(), left, right, binary.getOperationRange(), binary.getProperty(), binary.getComment());
        return null;
    }

    @Override
    public Void visitMathRandomIntFunct(MathRandomIntFunct<Void> mathRandomIntFunct) {
        List<Expr<Void>> newParam = new ArrayList<>();
        mathRandomIntFunct.getParam().forEach(voidExpr -> {
            voidExpr.accept(this);
            newParam.add(getLastReplacedAsExpr());
        });
        this.lastReplacedPhrase = MathRandomIntFunct.make(newParam, mathRandomIntFunct.getProperty(), mathRandomIntFunct.getComment());
        return null;
    }

    @Override
    public Void visitNumConst(NumConst<Void> numConst) {
        // TODO create copies as well?
        this.lastReplacedPhrase = numConst;
        return null;
    }

    @Override
    public Void visitMathConst(MathConst<Void> mathConst) {
        this.lastReplacedPhrase = mathConst;
        return null;
    }
}
