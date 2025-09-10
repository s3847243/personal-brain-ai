import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { FileText, Search, Link2, Zap, Shield } from "lucide-react";

const features = [
  {
    icon: FileText,
    title: "Smart Document Ingestion",
    description: "Upload PDFs, text files, and URLs. Our AI automatically chunks and processes your content for optimal search and retrieval.",
    highlight: "Support for multiple file formats"
  },
  {
    icon: Search,
    title: "Semantic Search (RAG)",
    description: "Ask natural language questions about your knowledge base. Get contextual answers powered by advanced vector search and GPT.",
    highlight: "Understands meaning, not just keywords"
  },
  
  {
    icon: Link2,
    title: "Auto-Backlinking",
    description: "Discover hidden connections between your notes. Our AI automatically finds and suggests related content.",
    highlight: "Build a web of connected knowledge"
  },
  {
    icon: Zap,
    title: "Real-time Processing",
    description: "Background workers and queuing system ensure fast, reliable processing of your documents without delays.",
    highlight: "Powered by RabbitMQ and Redis"
  },
  {
    icon: Shield,
    title: "Secure & Private",
    description: "Your data is encrypted and stored securely. User-based isolation ensures your knowledge remains private.",
    highlight: "JWT authentication with refresh tokens"
  }
];

export const Features = () => {
  return (
    <section id="features" className="py-24 px-6">
      <div className="max-w-7xl mx-auto">
        {/* Section Header */}
        <div className="text-center mb-16 space-y-4">
          <h2 className="text-4xl lg:text-5xl font-bold text-card-foreground">
            Powerful Features for
            <span className="bg-gradient-neural bg-clip-text text-transparent"> Smart Knowledge</span>
          </h2>
          <p className="text-xl text-muted-foreground max-w-3xl mx-auto">
            Built with cutting-edge AI technology to give you the most advanced personal knowledge management experience.
          </p>
        </div>

        {/* Features Grid */}
        <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-8">
          {features.map((feature) => (
            <Card key={feature.title} className="group hover:shadow-glow transition-all duration-300 hover:-translate-y-1 border-border/50 bg-card/50 backdrop-blur">
              <CardHeader className="space-y-4">
                <div className="w-12 h-12 rounded-lg bg-gradient-neural flex items-center justify-center group-hover:scale-110 transition-transform duration-300">
                  <feature.icon className="w-6 h-6 text-primary-foreground" />
                </div>
                <div>
                  <CardTitle className="text-xl mb-2">{feature.title}</CardTitle>
                  <div className="inline-block px-2 py-1 text-xs rounded-full bg-primary/10 text-primary font-medium">
                    {feature.highlight}
                  </div>
                </div>
              </CardHeader>
              <CardContent>
                <CardDescription className="text-base leading-relaxed">
                  {feature.description}
                </CardDescription>
              </CardContent>
            </Card>
          ))}
        </div>
      </div>
    </section>
  );
};