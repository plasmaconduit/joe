package com.plasmaconduit.joe;

@SuppressWarnings({"ConstantConditions", "SwitchStatementWithTooFewBranches"})
public class Main {

    record Ping(ProcessId from) { }

    public static void main(String[] args) {
        var kernel = Kernel.makeDefault();
        kernel.spawn(pongProgram())
            .flatMap((processId) -> kernel.spawn(pingProgram(processId)))
            .unsafe()
            .accept((n) -> System.out.println("done"));
    }

    static Program pongProgram() {
        return Program.forever((os) ->
            os.receive((m) -> switch(m) {
                case Ping p -> os.println("ping!").then(os.send(p.from, "pong!"));
                default -> null;
            })
        );
    }

    static Program pingProgram(ProcessId target) {
        return Program.forever((os) ->
            os.send(target, new Ping(os.self()))
                .then(os.receive((m) -> switch (m) {
                    case String response -> os.println(response);
                    default -> null;
                }))
        );
    }

}
