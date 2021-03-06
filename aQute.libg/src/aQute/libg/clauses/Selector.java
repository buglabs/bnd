/*
 * $Header: /cvsroot/bnd/aQute.libg/src/aQute/libg/clauses/Selector.java,v 1.1 2009/01/19 14:17:42 pkriens Exp $
 * 
 * Copyright (c) OSGi Alliance (2006). All Rights Reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package aQute.libg.clauses;

import java.util.*;
import java.util.regex.*;

public class Selector {
	Pattern	pattern;
	String	instruction;
	boolean	negated;
	Clause	clause;

	public Selector(String instruction, boolean negated) {
		this.instruction = instruction;
		this.negated = negated;
	}

	public boolean matches(String value) {
		if (pattern == null) {
			pattern = Pattern.compile(instruction);
		}
		Matcher m = pattern.matcher(value);
		return m.matches();
	}

	public boolean isNegated() {
		return negated;
	}

	public String getPattern() {
		return instruction;
	}

	/**
	 * Convert a string based pattern to a regular expression based pattern.
	 * This is called an instruction, this object makes it easier to handle the
	 * different cases
	 * 
	 * @param string
	 * @return
	 */
	public static Selector getPattern(String string) {
		boolean negated = false;
		if (string.startsWith("!")) {
			negated = true;
			string = string.substring(1);
		}
		StringBuffer sb = new StringBuffer();
		for (int c = 0; c < string.length(); c++) {
			switch (string.charAt(c)) {
			case '.':
				sb.append("\\.");
				break;
			case '*':
				sb.append(".*");
				break;
			case '?':
				sb.append(".?");
				break;
			default:
				sb.append(string.charAt(c));
				break;
			}
		}
		string = sb.toString();
		if (string.endsWith("\\..*")) {
			sb.append("|");
			sb.append(string.substring(0, string.length() - 4));
		}
		return new Selector(sb.toString(), negated);
	}

	public String toString() {
		return getPattern();
	}

	public Clause getClause() {
		return clause;
	}

	public void setClause(Clause clause) {
		this.clause = clause;
	}

	public static List<Selector> getInstructions(Clauses clauses) {
		List<Selector> result = new ArrayList<Selector>();
		for (Map.Entry<String, Map<String, String>> entry : clauses.entrySet()) {
			Selector instruction = getPattern(entry.getKey());
			result.add(instruction);
		}
		return result;
	}
	
	public static <T> List<T> select(Collection<T> domain,
			List<Selector> instructions) {
		List<T> result = new ArrayList<T>();
		Iterator<T> iterator = domain.iterator(); 
		value: while (iterator.hasNext()) {
			T value = iterator.next();
			for (Selector instruction : instructions) {
				if (instruction.matches(value.toString())) {
					if (!instruction.isNegated())
						result.add(value);
					continue value;
				}
			}
		}
		return result;
	}
	

}
