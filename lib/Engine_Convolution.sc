Engine_Convolution : CroneEngine {
  var irBufL;
  var irBufR;
  var convolution;

  *new { arg context, doneCallback;
    ^super.new(context, doneCallback);
  }

  alloc {
    SynthDef(\Moonshine, {
      arg xfade, irL, irR;

      var sigL = SoundIn.ar(0);
      var sigR = SoundIn.ar(1);

      var verbL = Convolution2.ar(sigL, irL, 0, 512, 1);
      var verbR = Convolution2.ar(sigR, irR, 0, 512, 1);

      var outL = LinXFade2.ar(sigL, verbL, xfade);
      var outR = LinXFade2.ar(sigR, verbR, xfade);

      Out.ar(0, outL);
      Out.ar(1, outR);

    }).add;

    irBufL = Buffer.alloc(context.server, 2048);
    irBufR = Buffer.alloc(context.server, 2048);

    context.server.sync;

    convolution = Synth.new(\Moonshine, [\irL, irBufL, \irR, irBufR, \xfade, 50], target: context.xg);

    this.addCommand("onFile", "s", { arg msg;
      irBufL = irBufL.readChannel(msg[1], channels: [0]);
      irBufR = irBufL.readChannel(msg[1], channels: [1]);
      convolution.set(\irL, irBufL);
      convolution.set(\irR, irBufR);
    });

    this.addCommand("xfade", "f", { arg msg;
      convolution.set(\xfade, msg[1]);
    });

  }
}
