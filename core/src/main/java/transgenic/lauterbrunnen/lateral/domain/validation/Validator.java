package transgenic.lauterbrunnen.lateral.domain.validation;

/**
 * Created by stumeikle on 22/11/18.
 */
public interface Validator<T> {

    void validate(T value) throws ValidationException;
}
