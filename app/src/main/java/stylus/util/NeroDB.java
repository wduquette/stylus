package stylus.util;

import com.wjduquette.joe.Joe;
import com.wjduquette.joe.JoeError;
import com.wjduquette.joe.SourceBuffer;
import com.wjduquette.joe.nero.Fact;
import com.wjduquette.joe.nero.FactSet;
import com.wjduquette.joe.nero.Nero;
import com.wjduquette.joe.nero.Schema;
import stylus.DataFileException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * An experimental Nero-based in-memory database, for use from Java code.
 */
public class NeroDB extends FactSet {
    //-------------------------------------------------------------------------
    // Instance Variables

    // The instance of Nero used to execute Nero programs.
    private final Nero nero = new Nero(new Joe());

    private final Schema schema;

    //-------------------------------------------------------------------------
    // Constructor

    public NeroDB(String schemaScript) {
        var ruleSet = nero.compile(new SourceBuffer("*neroDB*", schemaScript));
        this.schema = ruleSet.schema();
        addAll(nero.execute(ruleSet).getKnownFacts());
    }

    //-------------------------------------------------------------------------
    // Public API

    public void load(Path path) throws DataFileException {
        var error = new JoeError("Schema errors in " + path);

        try {
            var script = Files.readString(path);
            var source = new SourceBuffer(path.getFileName().toString(), script);
            var ruleSet = nero.compile(source);
            for (var name : ruleSet.schema().getRelations()) {
                if (!schema.checkAndAdd(ruleSet.schema().get(name))) {
                    var message = "Schema mismatch, expected " +
                        schema.get(name).toSpec() + ", got: " +
                        ruleSet.schema().get(name).toSpec() + ".";
                    error.addFrame(message);
                }
            }

            if (!error.getTraces().isEmpty()) {
                throw error;
            }

            addAll(nero.execute(ruleSet).getKnownFacts());
        } catch (Exception ex) {
            throw switch (ex) {
                case IOException ignored -> new DataFileException(
                    "Error reading " + path, ex);
                case JoeError ignored -> new DataFileException(
                    ex.getMessage(), ex);
                default -> new DataFileException(
                    "Unexpected error while loading Nero file  " + path +
                    ", " + ex.getMessage(), ex);
            };
        }
    }

    public List<Fact> query(String query) throws DataFileException {
        try {
            var result = nero.execute(new SourceBuffer("*neroDB*", query), this);
            return new ArrayList<>(result.getInferredFacts());
        } catch (JoeError ex) {
            throw new DataFileException("Error in query.", ex);
        }
    }

    @SuppressWarnings("unused")
    public String toNero() {
        return nero.asNeroScript(this);
    }
}
