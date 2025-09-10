import { Button } from "@/components/ui/button";
import { ArrowRight, Brain, FileText, Search } from "lucide-react";
// import heroImage from "@/assets/hero-brain.jpg";

export const Hero = () => {
  return (
    <section className="relative min-h-screen flex items-center justify-center overflow-hidden">
      {/* Neural Background */}
      <div className="absolute inset-0 bg-gradient-brain opacity-50" />
      <div className="absolute inset-0 bg-gradient-glow opacity-30" />
      
      {/* Hero Content */}
      <div className="relative z-10 max-w-7xl mx-auto px-6 py-20">
        <div className="grid lg:grid-cols-2 gap-12 items-center">
          {/* Left Column - Text Content */}
          <div className="space-y-8">
            <div className="space-y-4">
              <div className="inline-flex items-center gap-2 px-4 py-2 rounded-full bg-primary/10 border border-primary/20">
                <Brain className="w-4 h-4 text-primary" />
                <span className="text-sm font-medium text-primary">AI-Powered Knowledge Management</span>
              </div>
              
              <h1 className="text-5xl lg:text-7xl font-bold leading-tight text-card-foreground">
                Your
                <span className="bg-gradient-neural bg-clip-text text-transparent"> Personal</span>
                <br />
                AI Brain
              </h1>
              
              <p className="text-xl text-muted-foreground leading-relaxed max-w-lg">
                Transform your documents, notes, and thoughts into an intelligent, searchable knowledge base. 
                Ask questions, discover connections, and never lose an insight again.
              </p>
            </div>

            <div className="flex flex-col sm:flex-row gap-4">
              <Button variant="outline" size="lg" className="group text-card-foreground">
                Get Started Free
                <ArrowRight className="w-5 h-5 group-hover:translate-x-1 transition-transform" />
              </Button>
              <Button variant="outline" size="lg" className="text-card-foreground">
                Watch Demo
              </Button>
            </div>

            {/* Feature Pills */}
            <div className="flex flex-wrap gap-3 pt-4 text-card-foreground">
              <div className="flex items-center gap-2 px-3 py-1 rounded-full bg-secondary/50 border border-border">
                <FileText className="w-4 h-4 text-accent" />
                <span className="text-sm">Smart Ingestion</span>
              </div>
              <div className="flex items-center gap-2 px-3 py-1 rounded-full bg-secondary/50 border border-border">
                <Search className="w-4 h-4 text-accent" />
                <span className="text-sm">Semantic Search</span>
              </div>
             
            </div>
          </div>

          {/* Right Column - Hero Image */}
          <div className="relative">
            <div className="relative rounded-2xl overflow-hidden shadow-neural">
             
              <div className="absolute inset-0 bg-gradient-neural opacity-20" />
            </div>
            
            {/* Floating Elements */}
            <div className="absolute -top-4 -right-4 w-20 h-20 bg-gradient-neural rounded-full blur-xl opacity-60 animate-pulse" />
            <div className="absolute -bottom-4 -left-4 w-16 h-16 bg-gradient-neural rounded-full blur-xl opacity-40 animate-pulse delay-1000" />
          </div>
        </div>
      </div>

      {/* Background Decorations */}
      <div className="absolute top-1/4 left-10 w-32 h-32 bg-gradient-neural rounded-full blur-3xl opacity-20 animate-pulse" />
      <div className="absolute bottom-1/4 right-10 w-24 h-24 bg-gradient-neural rounded-full blur-3xl opacity-15 animate-pulse delay-500" />
    </section>
  );
};