package shared;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public class ArgumentParser {

	// Integer: How many args are expected. -1 if undefined
	private HashMap<String, Integer> expected;
	private HashMap<String, String[]> received;

	protected static class InvalidArgsException extends Exception {

		private static final long serialVersionUID = 1L;

		public InvalidArgsException(String msg) {
			super(msg);
		}

	}

	public ArgumentParser() {
		expected = new HashMap<String, Integer>();
		expected.put("-h", 0);
		received = new HashMap<String, String[]>();
	}

	public void setArg(String flag, int num) {
		expected.put(flag, num);
	}

	public String[] getValues(String flag) {
		return received.get(flag);
	}

	public boolean containsFlag(String flag) {
		return received.containsKey(flag);
	}

	/**
	 * Valida cada um dos valores
	 * 
	 * @throws InvalidArgsException
	 */
	private void valid() throws InvalidArgsException {
		String valid = String.join("", expected.keySet());
		Predicate<String> validArg = Pattern.compile("^[a-zA-Z0-9.:/\\\\]*$|^'[^'][a-zA-Z0-9.:/\\\\]*'$").asPredicate();
		for (Entry<String, String[]> entry : received.entrySet()) {
			String flag = entry.getKey();
			String[] args = entry.getValue();
			if (!valid.contains(flag))
				throw new InvalidArgsException("Flag " + flag + ": não é válida.");
			if ((expected.get(flag) == -1 && args.length < 1))
				throw new InvalidArgsException("Flag " + flag + ": não recebeu valores.");
			else if ((expected.get(flag) != -1 && expected.get(flag) != args.length))
				throw new InvalidArgsException("Flag " + flag + ": esperados " + String.valueOf(expected.get(flag))
						+ " argumentos, foram recebidos " + received.get(flag).length + ".");
			for (String val : args) {
				if (!validArg.test(val))
					throw new InvalidArgsException("Flag " + flag + ": recebido argumento invalido: " + val);
			}
		}
	}

	/**
	 * 
	 * @param args
	 * @throws InvalidArgsException
	 */
	private void parser(String[] args) throws InvalidArgsException {
		if (args.length < 1) {
			throw new InvalidArgsException("No arguments received!");
		}
		Predicate<String> flagValidator = Pattern.compile("^-[a-z]{1,}$").asPredicate();
		String currflag = "";
		List<String> currargs = new ArrayList<String>();
		for (String arg : args) {
			if (arg.contentEquals(args[args.length - 1])) {
				if (currflag.isEmpty() && currargs.size() == 0) {
					throw new InvalidArgsException("Invalid arguments received.");
				} else {
					if(flagValidator.test(arg)) {
						received.put(arg, new String[0]);
					} else {
						currargs.add(arg);
						received.put(currflag, currargs.toArray(new String[0]));
					}
				}
			}
			if (flagValidator.test(arg)) {
				if (!currflag.isBlank()) {
					received.put(currflag, currargs.toArray(new String[0]));
					currargs = new ArrayList<String>();
				}
				currflag = arg;
			} else {
				currargs.add(arg);
			}
		}
	}

	public void parse(String[] args) throws InvalidArgsException {
		parser(args);
		valid();
	}

	@Override
	public String toString() {
		String result = "";
		for (String key : received.keySet()) {
			result += key + "=>" + String.join(",", received.get(key)) + "\n";
		}
		return result;
	}
}