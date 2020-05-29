package de.fhg.iais.roberta.worker;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import de.fhg.iais.roberta.components.ConfigurationComponent;
import de.fhg.iais.roberta.util.Pair;
import de.fhg.iais.roberta.util.dbc.DbcException;

public class DefaultConfigurationHelper {

    private static final Map<String, String> XML_TO_BLOCKLY_PIN_MAPPING = new HashMap<>();
    static {
        XML_TO_BLOCKLY_PIN_MAPPING.put("0", "P0");
        XML_TO_BLOCKLY_PIN_MAPPING.put("1", "P1");
        XML_TO_BLOCKLY_PIN_MAPPING.put("2", "P2");
        XML_TO_BLOCKLY_PIN_MAPPING.put("3", "P3");
        XML_TO_BLOCKLY_PIN_MAPPING.put("4", "A0");
        XML_TO_BLOCKLY_PIN_MAPPING.put("5", "A1");
        XML_TO_BLOCKLY_PIN_MAPPING.put("C04", "C04");
        XML_TO_BLOCKLY_PIN_MAPPING.put("C05", "C05");
        XML_TO_BLOCKLY_PIN_MAPPING.put("C06", "C06");
        XML_TO_BLOCKLY_PIN_MAPPING.put("C07", "C07");
        XML_TO_BLOCKLY_PIN_MAPPING.put("C08", "C08");
        XML_TO_BLOCKLY_PIN_MAPPING.put("C09", "C09");
        XML_TO_BLOCKLY_PIN_MAPPING.put("C10", "C10");
        XML_TO_BLOCKLY_PIN_MAPPING.put("C11", "C11");
        XML_TO_BLOCKLY_PIN_MAPPING.put("C12", "C12");
        XML_TO_BLOCKLY_PIN_MAPPING.put("C16", "C16");
        XML_TO_BLOCKLY_PIN_MAPPING.put("C17", "C17");
        XML_TO_BLOCKLY_PIN_MAPPING.put("C18", "C18");
        XML_TO_BLOCKLY_PIN_MAPPING.put("C19", "C19");
    }

    private static final Map<String, String> XML_TO_BLOCKLY_RGBLED_MAPPING = new HashMap<>();
    static {
        XML_TO_BLOCKLY_RGBLED_MAPPING.put("0", "R");
        XML_TO_BLOCKLY_RGBLED_MAPPING.put("1", "R_CB_LF");
        XML_TO_BLOCKLY_RGBLED_MAPPING.put("2", "R_CB_LR");
        XML_TO_BLOCKLY_RGBLED_MAPPING.put("3", "R_CB_RR");
        XML_TO_BLOCKLY_RGBLED_MAPPING.put("4", "R_CB_RF");
        XML_TO_BLOCKLY_RGBLED_MAPPING.put("5", "R_CB_A");
    }

    private static final Map<String, String> XML_TO_BLOCKLY_LED_MAPPING = new HashMap<>();
    static {
        XML_TO_BLOCKLY_LED_MAPPING.put("1", "L_CB_L");
        XML_TO_BLOCKLY_LED_MAPPING.put("2", "L_CB_R");
        XML_TO_BLOCKLY_LED_MAPPING.put("3", "L_CB_B");
    }

    private static final Map<String, String> XML_TO_BLOCKLY_ULTRASONIC_MAPPING = new HashMap<>();
    static {
        XML_TO_BLOCKLY_ULTRASONIC_MAPPING.put("1", "U");
        XML_TO_BLOCKLY_ULTRASONIC_MAPPING.put("2", "U_CB");
    }

    private static final Map<Pair<String, String>, String> PROG_BLOCK_TO_CONF_BLOCK_TYPE_MAPPING = new HashMap<>();
    static {
        PROG_BLOCK_TO_CONF_BLOCK_TYPE_MAPPING.put(Pair.of("COMPASS_SENSING", "ANGLE"), "COMPASS");
        PROG_BLOCK_TO_CONF_BLOCK_TYPE_MAPPING.put(Pair.of("LIGHT_SENSING", "VALUE"), "LIGHT");
        PROG_BLOCK_TO_CONF_BLOCK_TYPE_MAPPING.put(Pair.of("SOUND_SENSING", "SOUND"), "SOUND");
        PROG_BLOCK_TO_CONF_BLOCK_TYPE_MAPPING.put(Pair.of("TEMPERATURE_SENSING", "VALUE"), "TEMPERATURE");
        PROG_BLOCK_TO_CONF_BLOCK_TYPE_MAPPING.put(Pair.of("KEYS_SENSING", "PRESSED"), "KEY");
        PROG_BLOCK_TO_CONF_BLOCK_TYPE_MAPPING.put(Pair.of("PIN_WRITE_VALUE", "ANALOG"), "ANALOG_INPUT");
        PROG_BLOCK_TO_CONF_BLOCK_TYPE_MAPPING.put(Pair.of("PIN_WRITE_VALUE", "DIGITAL"), "DIGITAL_INPUT");
        PROG_BLOCK_TO_CONF_BLOCK_TYPE_MAPPING.put(Pair.of("LED_ON_ACTION", ""), "RGBLED");
        PROG_BLOCK_TO_CONF_BLOCK_TYPE_MAPPING.put(Pair.of("LIGHT_STATUS_ACTION", "OFF"), "RGBLED");
        PROG_BLOCK_TO_CONF_BLOCK_TYPE_MAPPING.put(Pair.of("LIGHT_ACTION", "ON"), "LED");
        PROG_BLOCK_TO_CONF_BLOCK_TYPE_MAPPING.put(Pair.of("LIGHT_ACTION", "OFF"), "LED");
        PROG_BLOCK_TO_CONF_BLOCK_TYPE_MAPPING.put(Pair.of("PIN_SET_PULL", "UP"), "DIGITAL_INPUT");
        PROG_BLOCK_TO_CONF_BLOCK_TYPE_MAPPING.put(Pair.of("PIN_SET_PULL", "DOWN"), "DIGITAL_INPUT");
        PROG_BLOCK_TO_CONF_BLOCK_TYPE_MAPPING.put(Pair.of("PIN_SET_PULL", "NONE"), "DIGITAL_INPUT");
        PROG_BLOCK_TO_CONF_BLOCK_TYPE_MAPPING.put(Pair.of("ULTRASONIC_SENSING", "DISTANCE"), "ULTRASONIC");
    }

    // every component needs a default value, location, etc -> loaded from default configuration
    // TODO define separate configuration to get this data from? do it differently?
    public static ConfigurationComponent getDefaultComponent(Collection<ConfigurationComponent> confComps, String progBlockType, String mode, String port) {
        String confType = PROG_BLOCK_TO_CONF_BLOCK_TYPE_MAPPING.get(Pair.of(progBlockType, mode));
        String validatedPort = validatePort(port, confType);
        List<ConfigurationComponent> comps = confComps.stream().filter(confComp -> confComp.getComponentType().equals(confType)).collect(Collectors.toList());
        if ( comps.size() > 1 ) { // a block that is allowed multiple times in the configuration
            return comps
                .stream()
                .filter(confComp -> confComp.getUserDefinedPortName().equals(validatedPort))
                .findFirst()
                .orElseThrow(() -> new DbcException("No default block exists for " + confType + " on port " + validatedPort + '!'));
        } else { // a block that can only exist once in the configuration
            return comps.stream().findFirst().orElseThrow(() -> new DbcException("No default block exists for confType " + confType + '!'));
        }
    }

    private static String validatePort(String port, String confType) {
        // the PIN_WRITE_VALUE block needs special behaviour
        // TODO this _D and _A is tied to how the blocks are named in the default configuration, do it differently?
        switch ( confType ) {
            case "DIGITAL_INPUT":
                return XML_TO_BLOCKLY_PIN_MAPPING.get(port) + "_D";
            case "ANALOG_INPUT":
                return XML_TO_BLOCKLY_PIN_MAPPING.get(port) + "_A";
            case "RGBLED":
                return XML_TO_BLOCKLY_RGBLED_MAPPING.get(port);
            case "LED":
                return XML_TO_BLOCKLY_LED_MAPPING.get(port);
            case "ULTRASONIC":
                return XML_TO_BLOCKLY_ULTRASONIC_MAPPING.get(port);
            default:
                return port;
        }
    }
}