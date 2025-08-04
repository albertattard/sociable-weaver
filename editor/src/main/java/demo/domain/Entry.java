package demo.domain;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
public sealed interface Entry permits Breakpoint, Command, DisplayFile, Heading, Markdown, Todo {}
