import { Card, CardContent } from "@/components/ui/card";
import { Upload, Brain, MessageSquare, ArrowRight } from "lucide-react";

const steps = [
  {
    number: "01",
    icon: Upload,
    title: "Upload Your Knowledge",
    description: "Drop in your PDFs, documents, and URLs. Our system intelligently processes and chunks your content.",
    color: "from-purple-500 to-indigo-500"
  },
  {
    number: "02", 
    icon: Brain,
    title: "AI Processing",
    description: "Advanced embeddings create semantic understanding of your content, building connections and context.",
    color: "from-indigo-500 to-blue-500"
  },
  {
    number: "03",
    icon: MessageSquare,
    title: "Ask & Discover",
    description: "Query your knowledge in natural language. Get instant, contextual answers with source references.",
    color: "from-blue-500 to-cyan-500"
  }
];

export const HowItWorks = () => {
  return (
    <section id="how-it-works" className="py-24 px-6 bg-gradient-brain">
      <div className="max-w-7xl mx-auto">
        {/* Section Header */}
        <div className="text-center mb-16 space-y-4">
          <h2 className="text-4xl lg:text-5xl font-bold text-card-foreground">
            How It
            <span className="bg-gradient-neural bg-clip-text text-transparent"> Works</span>
          </h2>
          <p className="text-xl text-muted-foreground max-w-2xl mx-auto">
            Transform your scattered knowledge into an intelligent, searchable brain in three simple steps.
          </p>
        </div>

        {/* Steps */}
        <div className="grid lg:grid-cols-3 gap-8">
          {steps.map((step, index) => (
            <div key={step.number} className="relative">
              <Card className="group hover:shadow-glow transition-all duration-300 hover:-translate-y-1 border-border/50 bg-card/50 backdrop-blur">
                <CardContent className="p-8 text-center space-y-6">
                  {/* Step Number */}
                  <div className="text-6xl font-bold text-muted-foreground/20">
                    {step.number}
                  </div>
                  
                  {/* Icon */}
                  <div className={`w-16 h-16 mx-auto rounded-xl bg-gradient-to-r ${step.color} flex items-center justify-center group-hover:scale-110 transition-transform duration-300`}>
                    <step.icon className="w-8 h-8 text-white" />
                  </div>

                  {/* Content */}
                  <div className="space-y-3">
                    <h3 className="text-2xl font-bold">{step.title}</h3>
                    <p className="text-muted-foreground leading-relaxed">
                      {step.description}
                    </p>
                  </div>
                </CardContent>
              </Card>

              {/* Arrow (except for last item) */}
              {index < steps.length - 1 && (
                <div className="hidden lg:block absolute top-1/2 -right-4 transform -translate-y-1/2 z-10">
                  <div className="w-8 h-8 rounded-full bg-gradient-neural flex items-center justify-center">
                    <ArrowRight className="w-4 h-4 text-primary-foreground" />
                  </div>
                </div>
              )}
            </div>
          ))}
        </div>
      </div>
    </section>
  );
};