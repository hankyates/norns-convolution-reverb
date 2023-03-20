Engine_Convolution : CroneEngine {
  var bufsize, irL, irR, convolution;

  *new { arg context, doneCallback;
    ^super.new(context, doneCallback);
  }

  alloc {

    ~fftsize = 1024;

    irL = Buffer.readChannel(context.server, "/home/we/dust/code/convolution-reverb/assets/fort-flagler-powder-room-stereo.wav", channels: [0]);
    irR = Buffer.readChannel(context.server, "/home/we/dust/code/convolution-reverb/assets/fort-flagler-powder-room-stereo.wav", channels: [1]);

    context.server.sync;

    bufsize = PartConv.calcBufSize(~fftsize, irL);

    ~irBufL = Buffer.alloc(context.server, bufsize, 1);
    ~irBufL.preparePartConv(irL, ~fftsize);

    ~irBufR = Buffer.alloc(context.server, bufsize, 1);
    ~irBufR.preparePartConv(irR, ~fftsize);

    context.server.sync;

    irL.free;
    irR.free;

    SynthDef(\conv, {
      arg dry, wet;

      var sigL = SoundIn.ar(0);
      var sigR = SoundIn.ar(1);

      var verbL = PartConv.ar(sigL, ~fftsize, ~irBufL.bufnum);
      var verbR = PartConv.ar(sigR, ~fftsize, ~irBufR.bufnum);

      var outL = Mix([sigL * dry, verbL * wet * 0.2]);
      var outR = Mix([sigR * dry, verbR * wet * 0.2]);

      Out.ar(0, [outL, outR]);

    }).add;

    convolution = Synth.new(\conv, [
      \dry, 0,
      \wet, 0
    ], target: context.xg);

    this.addCommand("dry", "f", { arg msg;
      convolution.set(\dry, msg[1]);
    });

    this.addCommand("wet", "f", { arg msg;
      convolution.set(\wet, msg[1]);
    });

  }
}
