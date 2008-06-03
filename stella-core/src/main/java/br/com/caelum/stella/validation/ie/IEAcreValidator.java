package br.com.caelum.stella.validation.ie;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import br.com.caelum.stella.MessageProducer;
import br.com.caelum.stella.SimpleMessageProducer;
import br.com.caelum.stella.constraint.IEConstraints;
import br.com.caelum.stella.validation.BaseValidator;
import br.com.caelum.stella.validation.DigitoVerificadorInfo;
import br.com.caelum.stella.validation.InvalidValue;
import br.com.caelum.stella.validation.RotinaDeDigitoVerificador;
import br.com.caelum.stella.validation.ValidadorDeDV;
import br.com.caelum.stella.validation.error.IEError;

public class IEAcreValidator extends BaseValidator<String> {

	private static final int MOD = 11;
	private static final RotinaDeDigitoVerificador[] rotinas = {
		IEConstraints.Rotina.E, IEConstraints.Rotina.POS_IE };

	private static final int DVX_POSITION = 1 + 13;
	private static final Integer[] DVX_MULTIPLIERS = IEConstraints.P1;
	private static final DigitoVerificadorInfo DVX_INFO = new DigitoVerificadorInfo(
			0, rotinas, MOD, DVX_MULTIPLIERS, DVX_POSITION);
	private static final ValidadorDeDV DVX_CHECKER = new ValidadorDeDV(DVX_INFO);

	private static final int DVY_POSITION = 1 + 12;
	private static final Integer[] DVY_MULTIPLIERS = IEConstraints.P2;
	private static final DigitoVerificadorInfo DVY_INFO = new DigitoVerificadorInfo(
			0, rotinas, MOD, DVY_MULTIPLIERS, DVY_POSITION);
	private static final ValidadorDeDV DVY_CHECKER = new ValidadorDeDV(DVY_INFO);
	
	private final boolean isFormatted;

	/*
	 * Formato: 11 dígitos+2 dígitos verificadores
	 * 
	 * Os primeiros dois dígitos são sempre 01
	 * 
	 * Exemplo: Inscrição Estadual 01.004.823/001-12
	 * 
	 * 01.004.141/001-46
	 * 
	 * 01.001.349/001-77
	 * 
	 */
	public static final Pattern FORMATED = Pattern
			.compile("(01)[.](\\d{3})[.](\\d{3})[/](\\d{3})[-](\\d{2})");
	public static final Pattern UNFORMATED = Pattern
			.compile("(01)(\\d{3})(\\d{3})(\\d{3})(\\d{2})");

	 /**
     * Este considera, por padrão, que as cadeias estão formatadas e utiliza um
     * {@linkplain SimpleMessageProducer} para geração de mensagens.
     */
    public IEAcreValidator() {
        this(true);
    }

    /**
     * O validador utiliza um {@linkplain SimpleMessageProducer} para geração de
     * mensagens.
     * 
     * @param isFormatted
     *                considerar cadeia formatada quando <code>true</code>
     */
    public IEAcreValidator(boolean isFormatted) {
        this.isFormatted = isFormatted;
    }
	
	
	public IEAcreValidator(MessageProducer messageProducer,
			boolean isFormatted) {
		super(messageProducer);
		this.isFormatted = isFormatted;
	}

	@Override
	protected List<InvalidValue> getInvalidValues(String IE) {
		List<InvalidValue> errors = new ArrayList<InvalidValue>();
		errors.clear();
		if (IE != null) {
			String unformatedIE = checkForCorrectFormat(IE, errors);
			if (errors.isEmpty()) {
				if (!hasValidCheckDigits(unformatedIE)) {
					errors.add(IEError.INVALID_CHECK_DIGITS);
				}
			}
		}
		return errors;
	}

	private String checkForCorrectFormat(String ie, List<InvalidValue> errors) {
		String unformatedIE = null;
		if (isFormatted) {
			if (!(FORMATED.matcher(ie).matches())) {
				errors.add(IEError.INVALID_FORMAT);
			}
			unformatedIE = ie.replaceAll("\\D", "");
		} else {
			if (!UNFORMATED.matcher(ie).matches()) {
				errors.add(IEError.INVALID_DIGITS);
			}
			unformatedIE = ie;
		}
		return unformatedIE;
	}

	private boolean hasValidCheckDigits(String value) {
		String testedValue = IEConstraints.PRE_VALIDATION_FORMATTER.format(value);
		return DVX_CHECKER.isDVValid(testedValue) && DVY_CHECKER.isDVValid(testedValue);
	}

}