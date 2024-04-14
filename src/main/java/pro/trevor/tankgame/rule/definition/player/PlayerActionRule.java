package pro.trevor.tankgame.rule.definition.player;

import org.json.JSONObject;
import pro.trevor.tankgame.rule.type.IPlayerElement;
import pro.trevor.tankgame.state.State;
import pro.trevor.tankgame.util.*;
import pro.trevor.tankgame.util.function.IVarTriConsumer;
import pro.trevor.tankgame.util.function.IVarTriPredicate;
import pro.trevor.tankgame.util.range.TypeRange;

import java.util.Arrays;

public class PlayerActionRule<T extends IPlayerElement> implements IPlayerRule<T> {

    private final String name;
    private final IVarTriPredicate<State, T, Object> predicate;
    private final IVarTriConsumer<State, T, Object> consumer;
    private final TypeRange<?>[] parameters;

    public PlayerActionRule(String name, IVarTriPredicate<State, T, Object> predicate, IVarTriConsumer<State, T, Object> consumer, TypeRange<?>... parameters) {
        this.name = name;
        this.predicate = predicate;
        this.consumer = consumer;
        this.parameters = parameters;
    }

    @Override
    public void apply(State state, T subject, Object... meta) {
        if (canApply(state, subject, meta)) {
            consumer.accept(state, subject, meta);
        } else {
            JSONObject error = new JSONObject();
            error.put("error", true);
            error.put("rule", name);

            if (subject instanceof IJsonObject subjectJson) {
                error.put("subject", subjectJson.toJson());
            } else {
                error.put("subject", subject.getPlayer());
            }

            System.err.println(error.toString(2));
            throw new Error(String.format("Failed to apply `%s` to `%s` given `%s`", name, subject, Arrays.toString(meta)));
        }
    }

    @Override
    public boolean canApply(State state, T subject, Object... meta) {
        return validateOptionalTypes(meta) && predicate.test(state, subject, meta);
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public TypeRange<?>[] parameters() {
        return parameters;
    }

    private boolean validateOptionalTypes(Object[] meta) {
        if (meta.length != parameters.length) {
            return false;
        }
        for (int i = 0; i < parameters.length; ++i) {
            if (!meta[i].getClass().equals(parameters[i].getBoundClass())) {
                return false;
            }
        }
        return true;
    }
}
