package com.eka.connect.creditrisk.util;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

public class YNBooleanDeserializer extends JsonDeserializer<Boolean> {

	protected static final String Y = "Y";
	protected static final String N = "N";

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.fasterxml.jackson.databind.JsonDeserializer#deserialize(com.fasterxml
	 * .jackson.core.JsonParser,
	 * com.fasterxml.jackson.databind.DeserializationContext)
	 */
	@Override
	public Boolean deserialize(JsonParser jp, DeserializationContext ctxt)
			throws IOException, JsonProcessingException {
		JsonToken currentToken = jp.getCurrentToken();

		if (currentToken.equals(JsonToken.VALUE_STRING)) {
			String text = jp.getText().trim();

			if (Y.equalsIgnoreCase(text)) {
				return Boolean.TRUE;
			} else if (N.equalsIgnoreCase(text)) {
				return Boolean.FALSE;
			}

			return Boolean.FALSE;
		} else if (currentToken.equals(JsonToken.VALUE_NULL)) {
			return getNullValue();
		}

		throw ctxt.mappingException(Boolean.class);
	}

	@Override
	public Boolean getNullValue() {
		return Boolean.FALSE;
	}

}
