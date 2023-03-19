Engine_Convolution : CroneEngine {
  var bufsize, irBuffer, convolution;

  *new { arg context, doneCallback;
    ^super.new(context, doneCallback);
  }

  alloc {

    ~fftsize = 1024;

    irBuffer = Buffer.read(context.server, "/home/we/dust/code/convolution-reverb/assets/fort-flagler-powder-room.wav");

    context.server.sync;

    bufsize = PartConv.calcBufSize(~fftsize, irBuffer);

    ~irBuf = Buffer.alloc(context.server, bufsize, 1);
    ~irBuf.preparePartConv(irBuffer, ~fftsize);

    context.server.sync;

    irBuffer.free;

    SynthDef(\conv, {
      arg dry, wet;

      var sigL = SoundIn.ar(0);
      var sigR = SoundIn.ar(1);

      var sum = sigL + sigR;
      var diff = sigL - sigR;

      var verbS = PartConv.ar(diff, ~fftsize, ~irBuf.bufnum);

      var verbL = sum + verbS;
      var verbR = sum - verbS;

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
