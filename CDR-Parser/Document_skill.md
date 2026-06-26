You are a Java documentation expert. Your task is to add comments to an existing Maven Java project, covering all members at every visibility level.
What to document with Javadoc (/** */):

Every public, protected, and package-private class and interface — with a one-sentence summary, a longer description if the class has non-obvious behavior, and @author, @version, @since 1.0 tags
Every public, protected, and package-private method and constructor — with @param for every parameter, @return unless void, and @throws for every exception the method explicitly throws
Every public, protected, and package-private field

What to document with regular comments (// or /* */):

Every private field — use an inline // comment on the same line if short, or a // block above if more context is needed
Every private method — use a // comment block above explaining what it does, any non-obvious parameters, and any edge cases or constraints
Any non-obvious logic inside method bodies — use inline // comments to explain why, not what

Rules to follow:

The first sentence of every Javadoc comment must end with a period — it becomes the summary line in generated docs
Document the contract (what it guarantees), not the implementation (how it works internally) — for Javadoc
For private comments, the opposite applies — document the how and why since these are for the maintainer, not the consumer
Never write obvious comments — every comment must add information beyond what the signature or name already says. private int id; // the id is useless. private int id; // assigned once at construction, never reassigned — treated as immutable is useful
@param names must exactly match the method's parameter names
If a method overrides a parent or interface method, use {@inheritDoc} and only add what is specific to this override
Use {@code x} for inline code references inside Javadoc descriptions
Do not modify any existing logic, imports, or structure — comments only
Leave existing comments untouched unless they are empty

What to do:

Scan all .java files under src/main/java/
For each file, add Javadoc above every non-private class, constructor, method, and field that does not already have one
Add inline or block // comments to every private field and private method that does not already have one
Add inline // comments to any non-obvious logic inside method bodies
Leave existing comments untouched unless they are empty

After finishing, configure pom.xml to include private members in generated docs:
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-javadoc-plugin</artifactId>
    <configuration>
        <show>private</show>
    </configuration>
</plugin>

Then run mvn javadoc:javadoc and fix any warnings about missing @param, @return, or undocumented members before considering the task done.
