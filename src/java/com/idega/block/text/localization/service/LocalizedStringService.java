package com.idega.block.text.localization.service;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface LocalizedStringService {

	public Set<String> getKeys(String identifier, String locale, List<String> sources);

	public Map<String, String> getLocalizations(Set<String> keys, String identifier, String locale, List<String> sources);

}